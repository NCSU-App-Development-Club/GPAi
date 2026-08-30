---
layout: ../layouts/PrivacyLayout.astro
title: "GPAi - Privacy Policy"
---

<p class="effective">Effective date: August 29, 2026</p>

GPAi ("we", "us", or "our") is an academic assistant app built for NC State University
students by the [App Development Club at NC State](https://appdevncsu.org/). This Privacy
Policy explains what data we collect, how we use it, and your rights in relation to that
data. By using GPAi, you agree to the practices described here.

## 1. Data We Collect

### Account Information

When you sign in with your Google account, we receive and store the following from
Google's identity verification:

- Your name (as it appears on your Google account)
- Your email address
- Your Google user identifier (a unique, opaque ID)

We restrict sign-in to **@ncsu.edu** email addresses only. We do not collect
any contacts or other Google account data. Your Google profile photo URL is included
in the verification data from Google, but we do not store it on our servers. We only
store it locally on your device to display it in the app.

### Academic Transcript Data

You may optionally upload an unofficial PDF transcript from the NC State MyPack Portal.
The app parses this file locally on your device to extract:

- Term names (e.g., "Fall 2023")
- Course codes, names, grades, credit hours, and grade points

This data is stored locally in an on-device database and is used solely for GPA calculation
on the device. **Your transcript data is never sent anywhere unless you actively send a
message in the AI advisor feature.** If you only use the GPA calculator and do not use the
advisor, your transcript data remains entirely on your device and is not transmitted to
any server or third party.

### Chat Messages and AI Advisor

When you send a message in the AI advisor, your message is sent to Google Gemini via
Cloudflare AI Gateway to generate a response. In addition to your message, the following
are also sent with each advisor request:

- **Your full academic transcript** (all courses, grades, and GPA) as context for the AI
- **Up to 20 of your most recent chat messages** in the current conversation
- A system prompt instructing the AI to act as an academic assistant

Your name and email are **not** automatically included in messages sent to the AI model.
However, if you voluntarily include this information in your messages, it will be visible
to the AI service.

**Because we use a free-tier API key, Google may retain all prompts and responses —
including your full transcript — and use them to improve Google products and services,
including machine learning.** See Google's
[Gemini API Additional Terms of Service](https://ai.google.dev/gemini-api/terms) for
details. Your messages and transcript are also routed through
[Cloudflare AI Gateway](https://developers.cloudflare.com/ai-gateway/), which may log
request metadata for analytics and caching purposes.

### Rate Limiting Data

To prevent abuse, we track the number of chat requests you make per day and per minute.
This uses only your opaque Google user identifier and the current timestamp — no message
content or personal information is stored for rate limiting.

### Content Reports

You may flag an AI-generated response as offensive, inaccurate, or misleading using the
in-app report feature. When you submit a report, the following data is stored on our servers
for up to 90 days:

- The flagged message content
- Your name, email address, and Google user identifier
- The reason you selected for the report
- A timestamp of when the report was submitted

Reports are reviewed by the GPAi development team to improve AI safety and content quality.
Flagged message content is not shared with third parties except as necessary for review by
the development team.

## 2. How We Use Your Data

We use your data for the following purposes only:

- **Authentication** — verifying your identity via Google and maintaining your session.
- **AI Advisor** — sending your chat messages and transcript context to an AI language
  model to generate academic responses.
- **GPA Calculation** — computing your GPA from parsed transcript data entirely on your
  device.
- **Abuse Prevention** — rate limiting to protect the service from excessive usage.
- **Content Safety** — storing content reports to review and improve AI response quality
  and safety.

## 3. Third-Party Services

We use the following third-party services in connection with your data:

- **Google Sign-In** — for authentication. Google's privacy policy applies to the sign-in
  flow: [Google Privacy Policy](https://policies.google.com/privacy).
- **Cloudflare** — our backend is hosted on Cloudflare Workers. Chat messages are routed
  through Cloudflare AI Gateway to reach Google Gemini. We also use Cloudflare Rate
  Limiting to enforce per-minute request limits. Cloudflare's privacy policy applies to
  their handling of network traffic and rate-limiting data:
  [Cloudflare Privacy Policy](https://www.cloudflare.com/privacypolicy/).
- **Google Gemini** — the AI language model that generates advisor responses. Chat messages
  and transcript context are sent to Google's Gemini API via Cloudflare AI Gateway.
  We use a free-tier API key, under which Google may retain prompts and use them to improve
  Google products and services, including machine learning. See Google's
  [Gemini API Additional Terms of Service](https://ai.google.dev/gemini-api/terms) for
  details on how data submitted to the free tier is handled, and Google's
  [Privacy Policy](https://policies.google.com/privacy) for general privacy practices.
- **Discord** — we use a Discord webhook to receive notifications when content is flagged.
  The webhook receives only the flag reason and a unique ID associated with the report; it
  does not receive your name, email, chat message content, or other personal information.

We do not use any analytics, advertising, crash reporting, or telemetry services.

## 4. Data Retention

- **Server-side sessions** store your name, email, and Google user ID in Cloudflare Workers
  KV. Sessions are automatically deleted after 30 days. You can also sign out at any time,
  which immediately deletes your session from our servers.
- **Content reports** are stored in Cloudflare Workers KV for up to 90 days. Reports include
  the flagged message content, your name, email, Google user ID, and the report reason.
  Reports are automatically deleted after 90 days.
- **Chat messages and transcript data** are stored locally on your device. They are not
  retained on our servers after your session ends. However, messages sent to the AI advisor
  are processed by Google Gemini under their free-tier terms, meaning Google may retain
  prompts and use them for product improvement. See
  [How Google Uses Your Data](https://ai.google.dev/gemini-api/terms) for specifics.
- **Rate limit counters** expire at the end of each UTC day.
- Transcript data and your past chat messages may be included in your device's cloud backups
  if you have Android backup enabled.

## 5. Data Security

- Your session token is encrypted on your device using AES-256-GCM with a device-bound
  key managed by the Android Keystore.
- All communication between the app and our backend uses HTTPS.
- Server-side session tokens are stored in Cloudflare Workers KV with automatic expiration
  after 30 days.
- Note: transcript data and chat messages stored locally in the app's database are **not**
  encrypted at rest. They are protected only by your device's standard app sandboxing.

## 6. Your Rights and Choices

- **Sign Out** — You can sign out at any time from the app's top bar. This immediately
  deletes your server-side session data (name, email, and session token). Your local
  transcript data and chat history will remain on your device until you clear app data
  or uninstall.
- **Delete Account** — You can delete your account from the Profile tab. This permanently
  deletes your server-side session and any content reports you have filed. Rate-limit
  data expires automatically within 24 hours. You can also request account deletion
  by visiting [gpai.appdevncsu.org/delete-account](https://gpai.appdevncsu.org/delete-account)
  or by emailing ncsuappdevelopmentclub@gmail.com.
- **Local Data** — All academic and chat data is stored on your device. You can clear it at
  any time through your device's app settings, or by uninstalling the app.

## 7. Children's Privacy

GPAi is not directed at children under 13. We do not knowingly collect personal information
from anyone under 13 years of age. If you are under 13, please do not use this app.

## 8. Changes to This Policy

We may update this Privacy Policy from time to time. If we make material changes, we will
update the effective date at the top of this page. Continued use of GPAi after changes are
posted constitutes acceptance of the updated policy.

## 9. Contact Us

If you have questions about this Privacy Policy, please contact the NC State App
Development Club at [ncsuappdevelopmentclub@gmail.com](mailto:ncsuappdevelopmentclub@gmail.com).
