# Claude CLI OpenRouter Integration Setup

## ✅ Current Configuration

Your Claude CLI is configured to use OpenRouter API. This configuration is **ONLY for Claude CLI** (terminal tool), not for Cursor IDE.

## 📋 Configuration Details

The following environment variables are set in your `~/.zshrc`:

```bash
export ANTHROPIC_BASE_URL="https://openrouter.ai/api"
export ANTHROPIC_AUTH_TOKEN="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"
export ANTHROPIC_API_KEY=""
```

## 🚀 How to Use Claude CLI with OpenRouter

### Basic Usage

```bash
# Ask Claude a question
claude "What is Spring Boot?"

# Check connection status
claude /status

# Interactive mode (default)
claude

# Print mode (non-interactive)
claude -p "Write a Java function"
```

### Verify OpenRouter Connection

```bash
# Check if OpenRouter is configured
claude /status
```

This should show that Claude CLI is connected to OpenRouter.

### Test It Works

```bash
# Simple test
claude "Hello, can you hear me?"

# If it responds, OpenRouter integration is working!
```

## 🔍 How to Verify Environment Variables

Check that variables are loaded:

```bash
env | grep ANTHROPIC
```

You should see:
```
ANTHROPIC_BASE_URL=https://openrouter.ai/api
ANTHROPIC_AUTH_TOKEN=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
ANTHROPIC_API_KEY=
```

## 📝 Important Notes

### ✅ For Claude CLI:
- Environment variables are loaded from `~/.zshrc`
- Works automatically when you open a terminal
- Use `claude /status` to verify connection

### ❌ For Cursor IDE:
- **NOT configured** to use OpenRouter
- Cursor IDE will use its own default settings (likely direct Anthropic API)
- Launch Cursor normally (not from terminal) to avoid inheriting these variables
- Or configure Cursor's API settings separately in Cursor's preferences

## 🔄 Reload Configuration

If you update the `.zshrc` file, reload it:

```bash
source ~/.zshrc
```

## 🐛 Troubleshooting

### Claude CLI not using OpenRouter?

1. **Check variables are set:**
   ```bash
   echo $ANTHROPIC_BASE_URL
   echo $ANTHROPIC_AUTH_TOKEN
   ```

2. **Reload shell configuration:**
   ```bash
   source ~/.zshrc
   ```

3. **Test API key:**
   ```bash
   curl https://openrouter.ai/api/v1/models \
     -H "Authorization: Bearer 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"
   ```

4. **Check Claude CLI version:**
   ```bash
   claude --version
   ```

### Want to switch back to direct Anthropic API?

Edit `~/.zshrc` and change:
```bash
# Comment out OpenRouter config
# export ANTHROPIC_BASE_URL="https://openrouter.ai/api"
# export ANTHROPIC_AUTH_TOKEN="..."

# Add direct Anthropic API key
export ANTHROPIC_API_KEY="your-anthropic-key-here"
```

Then reload: `source ~/.zshrc`

## 📊 Monitor Usage

Check your OpenRouter usage:
- Dashboard: https://openrouter.ai/dashboard
- API Keys: https://openrouter.ai/keys
- Models: https://openrouter.ai/models

## 🎯 Summary

- ✅ Claude CLI → Uses OpenRouter (via environment variables)
- ❌ Cursor IDE → Uses its own settings (not OpenRouter)
- ✅ `/status` command works in Claude CLI
- ✅ Configuration is in `~/.zshrc`

