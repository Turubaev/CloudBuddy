import { createServer } from "node:http";

const port = Number(process.env.PORT || 8787);
const host = process.env.HOST || "127.0.0.1";
const apiKey = process.env.OPENAI_API_KEY || "";
const model = process.env.OPENAI_MODEL || "gpt-5.6-luna";
const requests = new Map();
const quotas = new Map();
const freeWelcomeLimit = Number(process.env.FREE_WELCOME_MESSAGES || 15);
const freeDailyLimit = Number(process.env.FREE_DAILY_MESSAGES || 5);
const plusDailyLimit = Number(process.env.PLUS_DAILY_MESSAGES || 30);
const allowUnverifiedPlus = process.env.ALLOW_UNVERIFIED_PLUS === "true";
const trustedPlusClients = new Set(
  String(process.env.TRUSTED_PLUS_CLIENTS || "").split(",").map((value) => value.trim()).filter(Boolean),
);

const personalities = {
  WARM: "Говори особенно тепло и бережно.",
  CHEERFUL: "Добавляй лёгкий добрый юмор, но не шути над болью пользователя.",
  CALM: "Отвечай коротко, спокойно и конкретно.",
  PLAYFUL: "Будь немного озорным, но оставайся уважительным.",
};

const crisisMarkers = [
  "хочу умереть", "не хочу жить", "покончить с собой", "самоубий",
  "навредить себе", "убить себя", "suicide", "kill myself", "self harm",
];

const crisisReply = "Мне очень важно, что ты написал об этом. Я не экстренная служба и не могу обеспечить твою безопасность. Если опасность непосредственная — позвони в местную экстренную службу или попроси человека рядом остаться с тобой. Отойди от всего, чем можно навредить себе, и прямо сейчас свяжись с тем, кому доверяешь.";

function json(response, status, body) {
  response.writeHead(status, {
    "content-type": "application/json; charset=utf-8",
    "cache-control": "no-store",
    "x-content-type-options": "nosniff",
  });
  response.end(JSON.stringify(body));
}

function rateLimited(ip) {
  const now = Date.now();
  const recent = (requests.get(ip) || []).filter((timestamp) => now - timestamp < 60_000);
  recent.push(now);
  requests.set(ip, recent);
  return recent.length > 20;
}

function quotaFor(payload, fallbackId) {
  const candidate = String(payload.installationId || "");
  const clientId = /^[a-zA-Z0-9_-]{8,128}$/.test(candidate) ? candidate : fallbackId;
  const requestedPlus = payload.requestedTier === "PLUS";
  const plus = trustedPlusClients.has(clientId) || (allowUnverifiedPlus && requestedPlus);
  const today = new Date().toISOString().slice(0, 10);
  const stored = quotas.get(clientId) || { welcomeUsed: 0, day: today, dailyUsed: 0 };
  if (stored.day !== today) {
    stored.day = today;
    stored.dailyUsed = 0;
  }
  const welcomeRemaining = plus ? 0 : Math.max(0, freeWelcomeLimit - stored.welcomeUsed);
  const limit = plus ? plusDailyLimit : (welcomeRemaining > 0 ? freeWelcomeLimit : freeDailyLimit);
  const remaining = plus
    ? Math.max(0, plusDailyLimit - stored.dailyUsed)
    : (welcomeRemaining > 0 ? welcomeRemaining : Math.max(0, freeDailyLimit - stored.dailyUsed));
  return {
    plan: plus ? "PLUS" : "FREE",
    remaining,
    limit,
    welcome: !plus && welcomeRemaining > 0,
    consume() {
      if (!plus && stored.welcomeUsed < freeWelcomeLimit) stored.welcomeUsed += 1;
      else stored.dailyUsed += 1;
      quotas.set(clientId, stored);
    },
  };
}

async function readBody(request) {
  const chunks = [];
  let size = 0;
  for await (const chunk of request) {
    size += chunk.length;
    if (size > 16_384) throw new Error("Body too large");
    chunks.push(chunk);
  }
  return JSON.parse(Buffer.concat(chunks).toString("utf8"));
}

function extractText(response) {
  return (response.output || [])
    .flatMap((item) => item.content || [])
    .filter((item) => item.type === "output_text" && typeof item.text === "string")
    .map((item) => item.text)
    .join("\n")
    .trim();
}

async function createCompanionReply(payload) {
  const message = String(payload.message || "").trim().slice(0, 1000);
  if (!message) throw new Error("Message is required");
  if (crisisMarkers.some((marker) => message.toLowerCase().includes(marker))) return crisisReply;

  const buddyName = String(payload.buddyName || "Луми").slice(0, 20);
  const personality = personalities[payload.personality] || personalities.WARM;
  const mood = String(payload.mood || "NEUTRAL");
  const history = Array.isArray(payload.history)
    ? payload.history
        .filter((item) => item && ["user", "assistant"].includes(item.role))
        .map((item) => ({ role: item.role, content: String(item.content || "").slice(0, 2000) }))
        .filter((item) => item.content)
        .slice(-12)
    : [];
  if (history.at(-1)?.role !== "user" || history.at(-1)?.content !== message) {
    history.push({ role: "user", content: message });
  }
  const instructions = [
    `Ты ${buddyName}, вымышленное живое облачко и эмоциональный ИИ-компаньон.`,
    personality,
    `Последняя отмеченная пользователем эмоция: ${mood}.`,
    "Отвечай по-русски, естественно, обычно 2–5 предложений.",
    "Не ставь диагнозы, не назначай лечение, не утверждай, что заменяешь психолога.",
    "Не формируй зависимость, не обвиняй пользователя за отсутствие и не требуй эксклюзивности.",
    "Сначала признай эмоцию, затем задай не больше одного вопроса или предложи одно маленькое действие.",
    "Если есть риск самоповреждения, прямо рекомендуй немедленно связаться с местной экстренной службой и человеком рядом.",
  ].join(" ");

  const openAIResponse = await fetch("https://api.openai.com/v1/responses", {
    method: "POST",
    headers: {
      authorization: `Bearer ${apiKey}`,
      "content-type": "application/json",
    },
    body: JSON.stringify({
      model,
      instructions,
      input: history,
      store: false,
      max_output_tokens: 350,
      reasoning: { effort: "none" },
      text: { verbosity: "low" },
    }),
  });
  const body = await openAIResponse.json();
  if (!openAIResponse.ok) {
    const detail = body?.error?.message || `OpenAI returned ${openAIResponse.status}`;
    throw new Error(detail);
  }
  const reply = extractText(body);
  if (!reply) throw new Error("OpenAI returned no text");
  return reply;
}

const server = createServer(async (request, response) => {
  if (request.method === "GET" && request.url === "/health") {
    return json(response, 200, { ok: true, model, configured: Boolean(apiKey) });
  }
  if (request.method !== "POST" || request.url !== "/v1/chat") {
    return json(response, 404, { error: "Not found" });
  }
  const ip = request.socket.remoteAddress || "unknown";
  if (rateLimited(ip)) return json(response, 429, { error: "Too many requests" });
  if (!apiKey) return json(response, 503, { error: "OPENAI_API_KEY is not configured" });

  try {
    const payload = await readBody(request);
    const quota = quotaFor(payload, ip);
    if (quota.remaining <= 0) {
      return json(response, 429, {
        error: "AI quota exhausted",
        code: "quota_exhausted",
        quota: { plan: quota.plan, remaining: 0, limit: quota.limit, welcome: quota.welcome },
      });
    }
    const reply = await createCompanionReply(payload);
    quota.consume();
    return json(response, 200, {
      reply,
      quota: {
        plan: quota.plan,
        remaining: Math.max(0, quota.remaining - 1),
        limit: quota.limit,
        welcome: quota.welcome,
      },
    });
  } catch (error) {
    console.error(error instanceof Error ? error.message : error);
    return json(response, 502, { error: "Unable to create a reply" });
  }
});

server.listen(port, host, () => {
  console.log(`CloudBuddy backend listening on http://${host}:${port}`);
});
