# OpenRouter Integration - Verification & Usage Guide

## ✅ Current Setup Status

Your environment variables are correctly configured:
- ✅ `ANTHROPIC_BASE_URL`: `https://openrouter.ai/api`
- ✅ `ANTHROPIC_AUTH_TOKEN`: Set (your OpenRouter API key)
- ✅ `ANTHROPIC_API_KEY`: Empty (as required)

## 🔍 How to Verify Setup in Cursor

### Method 1: Check Cursor's Status (Recommended)

1. **Open Cursor IDE**
2. **Open the AI chat panel** (usually with `Cmd+L` or `Ctrl+L`)
3. **Try asking a question** - If it works and uses OpenRouter, you're all set!

### Method 2: Check Terminal Launch

Since environment variables are loaded from your shell, **launch Cursor from terminal** to ensure it picks them up:

```bash
# Method 1: Using open command
open -a Cursor

# Method 2: If cursor is in PATH
cursor

# Method 3: Using full path
/Applications/Cursor.app/Contents/MacOS/Cursor
```

### Method 3: Verify Environment Variables in Terminal

Before launching Cursor, verify variables are set:
```bash
env | grep ANTHROPIC
```

You should see:
```
ANTHROPIC_BASE_URL=https://openrouter.ai/api
ANTHROPIC_AUTH_TOKEN=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
ANTHROPIC_API_KEY=
```

### Method 4: Test API Connection

You can test if OpenRouter API is accessible:
```bash
curl https://openrouter.ai/api/v1/models
```

If this returns a list of models, your network connection to OpenRouter is working.

## 🚀 How to Use OpenRouter Integration

### What Changed?

Once configured, **nothing changes in how you use Cursor** - it looks and works exactly the same! The difference is:

- **Before**: Cursor → Anthropic API → Claude only
- **Now**: Cursor → OpenRouter → Multiple AI models available

### Benefits of Using OpenRouter

1. **Multiple Models**: Access to various AI models (Claude, GPT-4, Llama, etc.)
2. **Cost Optimization**: Potentially better pricing or model selection
3. **Unified Interface**: Single API for multiple providers

### Normal Usage

Just use Cursor as you normally would:
- **AI Chat**: Press `Cmd+L` (Mac) or `Ctrl+L` (Windows/Linux)
- **Inline Suggestions**: Cursor will automatically suggest code as you type
- **Code Completions**: Tab to accept AI suggestions
- **Chat with Files**: Right-click files and select "Chat with [filename]"

### Selecting Models (If Available)

Some integrations allow you to specify which model to use. Check Cursor's settings:
1. Open Cursor Settings (`Cmd+,` or `Ctrl+,`)
2. Look for "AI" or "Model" settings
3. You might see options to select different models

## 🧪 Quick Test

Try this in Cursor to verify it's working:

1. **Create a new file** or open any code file
2. **Ask Cursor AI** (Cmd+L):
   ```
   Write a simple hello world function in Java
   ```
3. If it responds and generates code, the integration is working!

## 🐛 Troubleshooting

### Problem: Environment variables not working in Cursor

**Solution 1**: Launch Cursor from terminal
```bash
# Make sure variables are loaded
source ~/.zshrc

# Launch Cursor
open -a Cursor
```

**Solution 2**: Restart your Mac
This ensures all applications get the updated environment variables.

**Solution 3**: Check if variables are actually set
```bash
echo $ANTHROPIC_BASE_URL
echo $ANTHROPIC_AUTH_TOKEN
```

### Problem: Cursor still uses direct Anthropic API

**Check**:
1. Did you restart Cursor after setting variables?
2. Did you launch Cursor from terminal?
3. Are the variables correctly set in `~/.zshrc`?

**Fix**:
1. Update `~/.zshrc` if needed
2. Run `source ~/.zshrc`
3. Quit Cursor completely
4. Launch from terminal: `open -a Cursor`

### Problem: API errors or authentication failures

**Check**:
1. Is your OpenRouter API key valid? Test at https://openrouter.ai/keys
2. Do you have credits/balance in your OpenRouter account?
3. Check OpenRouter dashboard: https://openrouter.ai/dashboard

### Problem: Can't verify if it's working

**Simple test**:
1. Open Cursor
2. Start typing code
3. If you get AI suggestions, it's working!
4. The integration is transparent - you won't see a difference in behavior

## 📊 Monitoring Usage

Check your OpenRouter usage:
- **Dashboard**: https://openrouter.ai/dashboard
- **API Keys**: https://openrouter.ai/keys
- **Models & Pricing**: https://openrouter.ai/models

## 🔄 Switching Back to Direct Anthropic API

If you want to go back to direct Anthropic API:

1. Edit `~/.zshrc`:
   ```bash
   nano ~/.zshrc
   ```

2. Comment out or remove the OpenRouter lines:
   ```bash
   # export ANTHROPIC_BASE_URL="https://openrouter.ai/api"
   # export ANTHROPIC_AUTH_TOKEN="..."
   # export ANTHROPIC_API_KEY=""
   ```

3. Add your direct Anthropic API key:
   ```bash
   export ANTHROPIC_API_KEY="your-anthropic-key-here"
   ```

4. Reload: `source ~/.zshrc`
5. Restart Cursor

## 📝 Summary

**What you have now**:
- ✅ OpenRouter API key configured
- ✅ Environment variables set
- ✅ Cursor ready to use OpenRouter as backend

**How to use**:
- Just use Cursor normally - nothing changes!
- Launch from terminal for best results
- Check OpenRouter dashboard to monitor usage

**Next steps**:
1. Restart Cursor (launch from terminal)
2. Start coding - it should work automatically!
3. Monitor usage at openrouter.ai/dashboard

