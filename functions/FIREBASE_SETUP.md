# Firebase Functions Setup Guide for SOLVIT

This guide provides comprehensive instructions for setting up and configuring Firebase Cloud Functions for the SOLVIT notification system.

## Initial System Setup

### 1. Install Node.js v18

Firebase Cloud Functions requires Node.js version 18. We recommend using nvm (Node Version Manager) for installing Node.js:

```bash
# Install nvm (if not already installed)
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash

# Reload shell configuration
source ~/.bashrc  # or source ~/.zshrc for Zsh

# Install specific Node.js version
nvm install 18.20.5  # Latest LTS version of Node.js 18
nvm use 18.20.5

# Verify installation
node --version  # Should show v18.20.5
npm --version   # Should show v10.8.2
```

### 2. Clean Environment Setup

If you have previously worked with Firebase, clean your environment:

```bash
# Uninstall global Firebase Tools if present
sudo npm uninstall -g firebase-tools

# Verify clean state
npm list -g --depth=0  # Should only show npm and corepack
```

### 3. Required System Dependencies

- Git (latest version)
- Node.js v18.20.5 (EXACTLY this version)
- npm v10.8.2 (comes with Node.js 18.20.5)
- Python 3.x (for some npm packages)
- A code editor (VS Code recommended)

### 4. Firebase Project Access

Before proceeding:
1. Request access to the SOLVIT Firebase project from your team lead
2. Ensure you have a Google account with access to Firebase Console
3. Accept the Firebase project invitation sent to your email
4. Make sure you have the Blaze (pay-as-you-go) plan enabled

## Troubleshooting Initial Setup

### Node.js Installation Issues

1. **Wrong Node.js Version**
   ```bash
   # If you see a different version than 18.x
   nvm install 18.20.5
   nvm alias default 18.20.5
   ```

2. **npm Permission Errors**
   If you see errors like `EACCES` when installing packages globally:
   ```bash
   # Fix npm cache permissions
   sudo chown -R $(id -u):$(id -g) ~/.npm
   ```

3. **Permission Errors**
   ```bash
   # If you get EACCES errors with npm
   sudo chown -R $USER ~/.npm
   sudo chown -R $USER ~/.nvm
   ```

4. **nvm Command Not Found**
   ```bash
   # Add to your ~/.bashrc or ~/.zshrc:
   export NVM_DIR="$HOME/.nvm"
   [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
   [ -s "$NVM_DIR/bash_completion" ] && \. "$NVM_DIR/bash_completion"
   ```

### Firebase Tools Issues

1. **Previous Installation Conflicts**
   ```bash
   # If firebase-tools won't uninstall
   sudo rm -rf /usr/local/lib/node_modules/firebase-tools
   sudo rm -rf ~/.config/firebase
   ```

2. **Login Issues**
   ```bash
   # Clear Firebase credentials
   rm -rf ~/.config/configstore/firebase-tools.json
   ```

### System Requirements Verification

Run this check before proceeding:

```bash
# Check Node.js version
node --version  # Should be v18.20.5

# Check npm
npm --version   # Should be v10.8.2

# Check global packages
npm list -g --depth=0  # Should only show npm and corepack

# Check Python (if needed)
python3 --version  # Should be v3.x.x

# Check Git
git --version
```

## Post-Setup Steps

After running the setup script, you need to complete these manual steps:

### 1. Firebase Project Configuration

1. Open your `.env` file in the `functions` directory
2. Fill in your Firebase project details from [Firebase Console](https://console.firebase.google.com/):
   ```bash
   FIREBASE_PROJECT_ID=solvit-14cc1        # Your project ID
   FIREBASE_STORAGE_BUCKET=solvit-14cc1.appspot.com
   FIREBASE_MESSAGING_SENDER_ID=           # From Project Settings > Cloud Messaging
   ```
   The other configuration values can keep their defaults:
   ```bash
   FUNCTION_REGION=europe-west1
   FUNCTION_MEMORY=256MB
   FUNCTION_TIMEOUT=60s
   ```

### 2. Firebase Functions Initialization

1. Login to Firebase (this will open your browser):
   ```bash
   firebase login
   ```

2. Initialize Firebase Functions:
   ```bash
   firebase init functions
   ```

3. During the initialization process:
   - When asked about initializing/overwriting codebase: Choose "overwrite an existing one"
   - Select "default" codebase
   - Choose "TypeScript" as your preferred language
   - Say "Yes" to using ESLint
   - Important: Say "No" to ALL file overwrites (package.json, eslintrc.js, tsconfig.json, etc.)
     This preserves our custom configuration while properly initializing Firebase

### 3. Verify Setup

After completing these steps, verify your setup:

1. Check Firebase CLI version:
   ```bash
   firebase --version  # Should show v12.9.1
   ```

2. Verify project configuration:
   ```bash
   firebase projects:list  # Should show solvit-14cc1
   ```

3. Test the emulator:
   ```bash
   npm run serve
   ```

## Prerequisites

Before starting, ensure you have:

- Node.js v18.20.5 installed
- npm (comes with Node.js)
- A Firebase project with Blaze (pay-as-you-go) plan enabled
- Git installed
- Basic knowledge of TypeScript and Firebase

## Quick Start

1. Make the setup script executable and run it:
```bash
chmod +x setup_firebase.sh
./setup_firebase.sh
```

2. Configure your environment variables:
```bash
cp .env.example .env
# Edit .env with your Firebase project details
```

3. Start the emulator:
```bash
npm run serve
```

## Detailed Setup Guide

### 1. Firebase Project Setup

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select an existing one
3. Enable required services:
   - Cloud Functions
   - Cloud Messaging (FCM)
   - Firestore
   - Authentication
4. Upgrade to Blaze plan (required for external API calls)
5. Download your service account key:
   - Go to Project Settings > Service Accounts
   - Click "Generate New Private Key"
   - Save the file securely

### 2. Local Development Setup

1. Install dependencies:
```bash
npm install
```

2. Configure environment variables:
   - Copy `.env.example` to `.env`
   - Update with your Firebase project details:
     - `FIREBASE_PROJECT_ID`
     - `FIREBASE_STORAGE_BUCKET`
     - `FIREBASE_MESSAGING_SENDER_ID`

3. Initialize Firebase project:
```bash
firebase use your-project-id
```

### 3. Development Workflow

#### Starting the Emulator

```bash
npm run serve
```

This will:
- Build the TypeScript code
- Start the Firebase emulator suite
- Watch for file changes

#### Testing

1. Run all tests:
```bash
npm test
```

2. Watch mode for development:
```bash
npm run test:watch
```

#### Linting and Formatting

1. Check code style:
```bash
npm run lint
```

2. Fix code style issues:
```bash
npm run lint:fix
```

### 4. Deployment

1. Build the project:
```bash
npm run build
```

2. Deploy to Firebase:
```bash
npm run deploy
```

## Project Structure

```
functions/
├── src/
│   ├── index.ts              # Main entry point
│   ├── handlers/             # Function handlers
│   │   └── notificationHandler.ts
│   ├── utils/               # Utility functions
│   ├── types/               # TypeScript type definitions
│   └── config/              # Configuration files
├── test/                    # Test files
├── .env                     # Environment variables
├── .env.example            # Environment variables template
├── .eslintrc.js           # ESLint configuration
├── .prettierrc            # Prettier configuration
├── tsconfig.json          # TypeScript configuration
└── package.json           # Project dependencies
```

## Security Considerations

1. **Environment Variables**
   - Never commit `.env` file
   - Use different values for development and production
   - Regularly rotate sensitive credentials

2. **Authentication**
   - All functions verify authentication by default
   - Use appropriate security rules in Firestore

3. **Rate Limiting**
   - Implement rate limiting for production
   - Monitor function usage

## Troubleshooting

### Common Issues

1. **Emulator Connection Issues**
   ```
   Error: Could not connect to Firestore emulator
   ```
   Solution: Check if ports are available and not blocked by other processes

2. **Deployment Failures**
   ```
   Error: Functions deploy failed
   ```
   Solution: Check billing status and project permissions

3. **TypeScript Compilation Errors**
   ```
   Error TS2307: Cannot find module...
   ```
   Solution: Run `npm install` and check `tsconfig.json`

### Debug Logs

View Firebase function logs:
```bash
npm run logs
```

## Maintenance

1. **Regular Updates**
   ```bash
   npm update
   npm audit fix
   ```

2. **Monitoring**
   - Check Firebase Console for function performance
   - Monitor error rates
   - Review usage metrics

## Testing Guide

### Unit Tests

1. Create test files in `test/` directory
2. Run tests:
```bash
npm test
```

### Integration Tests

1. Start emulators:
```bash
npm run serve
```

2. Run integration tests:
```bash
npm run test:integration
```

## CI/CD Integration

The project includes GitHub Actions workflow for:
- Linting
- Testing
- Building
- Deploying (on main branch)

## Additional Resources

- [Firebase Functions Documentation](https://firebase.google.com/docs/functions)
- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [TypeScript Documentation](https://www.typescriptlang.org/docs)

## Support

For issues:
1. Check the troubleshooting guide
2. Search existing GitHub issues
3. Create a new issue with:
   - Error message
   - Steps to reproduce
   - Environment details
