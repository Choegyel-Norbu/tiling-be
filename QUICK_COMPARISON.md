# Quick Comparison: Claude CLI vs Cursor IDE

## 🎯 The Main Difference

You have **TWO different tools** that can both use OpenRouter:

### 1. **Claude CLI** (Terminal Tool) 
**What you already had installed**
- ✅ Version: 2.0.75 (Claude Code)
- ✅ Command: `claude`
- ✅ Location: Terminal/command line
- ✅ Has `/status` command
- ✅ Best for: Quick AI queries, terminal work

**Example usage:**
```bash
claude "What is Spring Boot?"
claude /status  # Check connection
```

### 2. **Cursor IDE** (Visual Code Editor)
**What we just configured with OpenRouter**
- ✅ Full IDE (like VS Code)
- ✅ Visual interface (windows, menus)
- ✅ AI chat built-in (Cmd+L)
- ✅ No `/status` command (doesn't need it)
- ✅ Best for: Writing code, developing projects

**Example usage:**
- Open Cursor app
- Edit files visually
- Press `Cmd+L` for AI chat
- Get code suggestions as you type

---

## 🔍 About the `/status` Error

The `/status` command you tried is for **Claude CLI**, not Cursor IDE!

### ✅ Use `/status` in Terminal:
```bash
claude /status
```

### ❌ Don't use `/status` in Cursor IDE
Cursor doesn't have this command - that's why you got an error.

---

## 💡 Simple Test

### Test Claude CLI (Terminal):
```bash
# Check if OpenRouter is working
claude /status

# Or ask a question
claude "Explain Java Spring Boot"
```

### Test Cursor IDE:
1. Open Cursor
2. Press `Cmd+L` 
3. Ask: "Write a hello world"
4. If it responds → OpenRouter is working!

---

## 📋 Summary

| | Claude CLI | Cursor IDE |
|---|-----------|------------|
| **Type** | Terminal tool | Visual IDE |
| **Command** | `claude` | Open app |
| **Has `/status`?** | ✅ Yes | ❌ No |
| **Best for** | Quick queries | Coding projects |
| **OpenRouter?** | ✅ Yes (via env vars) | ✅ Yes (via env vars) |

Both use the same OpenRouter API key we set up! 🎉

