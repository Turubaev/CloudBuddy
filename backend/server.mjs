import { createServer } from "node:http";

const port = Number(process.env.PORT || 8787);
const host = process.env.HOST || "127.0.0.1";
const apiKey = process.env.OPENAI_API_KEY || "";
const model = process.env.OPENAI_MODEL || "gpt-5.4-mini";
const requests = new Map();

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
      input: message,
      max_output_tokens: 350,
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
    const reply = await createCompanionReply(payload);
    return json(response, 200, { reply });
  } catch (error) {
    console.error(error instanceof Error ? error.message : error);
    return json(response, 502, { error: "Unable to create a reply" });
  }
});

server.listen(port, host, () => {
  console.log(`CloudBuddy backend listening on http://${host}:${port}`);
});
