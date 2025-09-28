# GPAi Backend

This is a Hono app deployed on Cloudflare Workers which uses Workers KV for session storage.

## Expected Environment Variables

- `GOOGLE_WEB_CLIENT_ID`- a Google OAuth Client ID for a web application
- `GOOGLE_ANDROID_CLIENT_ID`- a Google OAuth Client ID for the Android app
- `EXPECTED_HD_DOMAIN` - the expected domain that users' Google accounts must be a part of (e.g. `ncsu.edu`)
- `AI_GATEWAY_ACCOUNT_ID` - the account ID that contains the AI gateway
- `AI_GATEWAY_ID` - the ID of the AI gateway (based on the name you give it on the Cloudflare dashboard)
- `AI_GATEWAY_MODEL` - the AI model slug to use
- `AI_GATEWAY_TOKEN` - an authorization token for Cloudflare AI Gateway
- `SESSION_TTL` - the session timeout duration, in seconds
