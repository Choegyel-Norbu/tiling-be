# Cursor IDE OpenRouter Integration - Troubleshooting

## Issue: `/status` Command Error

If you're seeing an error like "ID error installing Cursor extension" when running `/status`, here are the steps to fix it:

## 🔧 Solution Steps

### Step 1: Ensure Cursor is Launched from Terminal

Cursor needs to inherit environment variables from your shell. Launch it this way:

```bash
# First, make sure variables are loaded
source ~/.zshrc

# Then launch Cursor from terminal
open -a Cursor
```

**Important**: Close any existing Cursor windows first, then launch from terminal.

### Step 2: Verify Environment Variables in Cursor

1. Open Cursor
2. Open the integrated terminal in Cursor (`Ctrl+`` or `Cmd+``)
3. Run:
   ```bash
   env | grep ANTHROPIC
   ```

You should see:
```
ANTHROPIC_BASE_URL=https://openrouter.ai/api
ANTHROPIC_AUTH_TOKEN=sk-or-v1-...
ANTHROPIC_API_KEY=
```

If these are NOT visible, Cursor didn't pick up the environment variables.

### Step 3: Clear Cursor's Cache (if needed)

If variables still aren't loading, clear Cursor's cache:

```bash
# Close Cursor completely first, then run:
rm -rf ~/Library/Application\ Support/Cursor/User/globalStorage/state.vscdb*

# Then restart Cursor from terminal
open -a Cursor
```

### Step 4: Alternative - Set Variables in Cursor's Launch Script

Create a script to ensure variables are set when launching Cursor:

```bash
# Create launch script
cat > ~/launch-cursor.sh << 'EOF'
#!/bin/zsh
source ~/.zshrc
export ANTHROPIC_BASE_URL="https://openrouter.ai/api"
export ANTHROPIC_AUTH_TOKEN="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"
export ANTHROPIC_API_KEY=""
open -a Cursor
EOF

chmod +x ~/launch-cursor.sh
```

Then always launch Cursor using:
```bash
~/launch-cursor.sh
```

## 📝 About the `/status` Command

**Important Note**: The `/status` command mentioned in OpenRouter documentation might be specific to:
- Claude Code CLI (command-line tool)
- Or a specific Cursor extension/plugin

If `/status` isn't working, you can verify the integration is working by:

1. **Simply using Cursor normally** - If AI features work, OpenRouter is likely connected
2. **Check Cursor's terminal** - Run `env | grep ANTHROPIC` to see if variables are loaded
3. **Test AI functionality** - Press `Cmd+L` and ask a question - if it responds, it's working!

## ✅ Quick Verification Checklist

- [ ] Environment variables are set in `~/.zshrc`
- [ ] Ran `source ~/.zshrc` after updating
- [ ] Launched Cursor from terminal using `open -a Cursor`
- [ ] Verified variables in Cursor's integrated terminal
- [ ] AI features work in Cursor (chat, completions)

## 🐛 If Still Not Working

1. **Check Cursor Settings**:
   - Open Settings (`Cmd+,`)
   - Search for "environment" or "env"
   - See if there's a way to manually set environment variables

2. **Check Cursor Version**:
   - Make sure you're on the latest version
   - Update if needed

3. **Try Different Launch Method**:
   ```bash
   # Method 1: Direct path
   /Applications/Cursor.app/Contents/MacOS/Cursor
   
   # Method 2: With explicit environment
   env ANTHROPIC_BASE_URL="https://openrouter.ai/api" \
      ANTHROPIC_AUTH_TOKEN="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" \
      ANTHROPIC_API_KEY="" \
      open -a Cursor
   ```

4. **Test API Key Directly**:
   ```bash
   curl https://openrouter.ai/api/v1/models \
     -H "Authorization: Bearer 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"
   ```
   
   If this returns a list of models, your API key works.

## 💡 Alternative: Use Cursor's Native Settings

If environment variables aren't being picked up, Cursor might have its own settings for API keys:
1. Open Cursor Settings (`Cmd+,`)
2. Search for "API" or "Anthropic"
3. Look for fields to enter API keys directly

## 🎯 Bottom Line

**You don't need `/status` to work** - if Cursor's AI features work (chat, completions), then OpenRouter integration is successful! The `/status` command might be from outdated documentation or a different tool.

