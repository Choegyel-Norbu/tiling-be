# OpenRouter API Setup for Cursor IDE

## Step 1: Get Your OpenRouter API Key

1. **Visit OpenRouter**: Go to https://openrouter.ai
2. **Sign Up/Login**: Create an account or log in to your existing account
3. **Navigate to API Keys**: Go to https://openrouter.ai/keys
4. **Create New Key**:
   - Click "Create Key" button
   - Give it a name (e.g., "Cursor IDE")
   - Copy the API key (it will start with `sk-or-v1-...`)
   - ⚠️ **Important**: Save it immediately, you won't be able to see it again!

## Step 2: Update Your .zshrc File

I've already added the configuration to your `~/.zshrc` file. You just need to:

1. **Open your `.zshrc` file**:
   ```bash
   nano ~/.zshrc
   ```
   Or use any editor you prefer (VS Code, Cursor, etc.)

2. **Find this section** (it's at the bottom):
   ```bash
   # OpenRouter API Configuration for Claude Code (Cursor)
   export ANTHROPIC_BASE_URL="https://openrouter.ai/api"
   export ANTHROPIC_AUTH_TOKEN="YOUR_OPENROUTER_API_KEY"
   export ANTHROPIC_API_KEY=""
   ```

3. **Replace `YOUR_OPENROUTER_API_KEY`** with your actual API key from Step 1:
   ```bash
   export ANTHROPIC_AUTH_TOKEN="sk-or-v1-your-actual-key-here"
   ```

4. **Save the file** (in nano: `Ctrl+X`, then `Y`, then `Enter`)

## Step 3: Reload Your Shell Configuration

After updating the file, reload your shell:

```bash
source ~/.zshrc
```

## Step 4: Verify Environment Variables

Check that the variables are set correctly:

```bash
echo $ANTHROPIC_BASE_URL
echo $ANTHROPIC_AUTH_TOKEN
```

You should see:
- First command: `https://openrouter.ai/api`
- Second command: Your API key (or `YOUR_OPENROUTER_API_KEY` if you haven't updated it yet)

## Step 5: Configure Cursor IDE

**Important**: For Cursor IDE to use these environment variables, you have two options:

### Option A: Launch Cursor from Terminal (Recommended)
Launch Cursor from your terminal so it inherits the environment variables:
```bash
open -a Cursor
```
Or if Cursor is in your PATH:
```bash
cursor
```

### Option B: Set in Cursor's Launch Configuration
1. Quit Cursor completely
2. Create/edit Cursor's launch environment (if available in Cursor settings)
3. Or restart your Mac to ensure all applications get the updated environment variables

## Step 6: Verify Connection in Cursor

According to the OpenRouter documentation, you can verify the connection by using the `/status` command in Claude Code/Cursor.

## Troubleshooting

### Environment variables not working in Cursor?
- Make sure you've replaced `YOUR_OPENROUTER_API_KEY` with your actual key
- Restart Cursor after setting the variables
- Launch Cursor from terminal using `open -a Cursor` or `cursor`
- Check that variables are set: `env | grep ANTHROPIC`

### Need to update your API key?
1. Get a new key from https://openrouter.ai/keys
2. Update the `ANTHROPIC_AUTH_TOKEN` in `~/.zshrc`
3. Run `source ~/.zshrc`
4. Restart Cursor

## Environment Variables Explained

- **ANTHROPIC_BASE_URL**: Tells Claude Code to route requests through OpenRouter's API endpoint
- **ANTHROPIC_AUTH_TOKEN**: Your OpenRouter API key for authentication
- **ANTHROPIC_API_KEY**: Set to empty string to prevent conflicts with direct Anthropic API usage

## Additional Resources

- OpenRouter Dashboard: https://openrouter.ai/dashboard
- OpenRouter API Docs: https://openrouter.ai/docs
- OpenRouter Models: https://openrouter.ai/models


