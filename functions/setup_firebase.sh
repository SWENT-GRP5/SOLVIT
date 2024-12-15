#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Required versions - matching user's environment
REQUIRED_NODE_VERSION="18.20.5"
REQUIRED_NPM_VERSION="10.8.2"
REQUIRED_FIREBASE_TOOLS_VERSION="12.9.1"  # Latest stable version

# Function to print status
print_status() {
    echo -e "${YELLOW}➡️ $1${NC}"
}

# Function to print success
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

# Function to print error and exit
print_error() {
    echo -e "${RED}❌ $1${NC}"
    exit 1
}

# Function to compare versions
version_compare() {
    if [[ $1 == $2 ]]; then
        return 0
    fi
    local IFS=.
    local i ver1=($1) ver2=($2)
    for ((i=${#ver1[@]}; i<${#ver2[@]}; i++)); do
        ver1[i]=0
    done
    for ((i=0; i<${#ver1[@]}; i++)); do
        if [[ -z ${ver2[i]} ]]; then
            ver2[i]=0
        fi
        if ((10#${ver1[i]} > 10#${ver2[i]})); then
            return 1
        fi
        if ((10#${ver1[i]} < 10#${ver2[i]})); then
            return 2
        fi
    done
    return 0
}

# Function to check and fix npm cache permissions
check_npm_permissions() {
    local npm_cache_dir="/Users/$(whoami)/.npm"
    local current_user=$(whoami)
    local current_user_id=$(id -u)
    local current_group_id=$(id -g)

    if [ ! -w "$npm_cache_dir" ] || [ "$(stat -f '%u' "$npm_cache_dir")" != "$current_user_id" ]; then
        print_status "Fixing npm cache permissions..."
        sudo chown -R "$current_user_id:$current_group_id" "$npm_cache_dir" || print_error "Failed to fix npm cache permissions. Please run: sudo chown -R $current_user_id:$current_group_id \"$npm_cache_dir\""
        print_success "npm cache permissions fixed"
    fi
}

print_status "Starting Firebase Functions setup for SOLVIT..."

# Check Node.js version
print_status "Checking Node.js version..."
if ! command -v node &> /dev/null; then
    print_error "Node.js is required but not installed. Please install Node.js v${REQUIRED_NODE_VERSION}"
fi

NODE_VERSION=$(node -v | cut -d 'v' -f 2)
if [[ "$NODE_VERSION" != "$REQUIRED_NODE_VERSION" ]]; then
    print_error "Node.js v${REQUIRED_NODE_VERSION} is required. Found v${NODE_VERSION}. Please install the correct version using:\n\nnvm install ${REQUIRED_NODE_VERSION}\nnvm use ${REQUIRED_NODE_VERSION}"
fi
print_success "Node.js v${REQUIRED_NODE_VERSION} found"

# Check npm version
print_status "Checking npm version..."
if ! command -v npm &> /dev/null; then
    print_error "npm is required but not installed"
fi

NPM_VERSION=$(npm -v)
if [[ "$NPM_VERSION" != "$REQUIRED_NPM_VERSION" ]]; then
    print_error "npm v${REQUIRED_NPM_VERSION} is required. Found v${NPM_VERSION}"
fi
print_success "npm v${REQUIRED_NPM_VERSION} found"

# Check and fix npm permissions
check_npm_permissions

# Install or update Firebase CLI
print_status "Installing/updating Firebase CLI..."
if command -v firebase &> /dev/null; then
    FIREBASE_VERSION=$(firebase --version 2>/dev/null || echo "ERROR")
    if [[ "$FIREBASE_VERSION" != "$REQUIRED_FIREBASE_TOOLS_VERSION" ]]; then
        print_status "Removing existing Firebase CLI version ${FIREBASE_VERSION}..."
        npm uninstall -g firebase-tools || print_error "Failed to uninstall existing Firebase CLI"
    fi
fi

print_status "Installing Firebase CLI v${REQUIRED_FIREBASE_TOOLS_VERSION}..."
npm install -g firebase-tools@${REQUIRED_FIREBASE_TOOLS_VERSION} || print_error "Failed to install Firebase CLI"

# Create necessary configuration files
print_status "Creating configuration files..."

# Create package.json if it doesn't exist
if [ ! -f package.json ]; then
    print_status "Creating package.json..."
    cat > package.json << EOL
{
  "name": "solvit-functions",
  "version": "1.0.0",
  "description": "Firebase Cloud Functions for SOLVIT",
  "main": "lib/index.js",
  "scripts": {
    "build": "tsc",
    "serve": "npm run build && firebase emulators:start --only functions",
    "shell": "npm run build && firebase functions:shell",
    "start": "npm run shell",
    "deploy": "firebase deploy --only functions",
    "logs": "firebase functions:log",
    "test": "jest"
  },
  "engines": {
    "node": "18"
  },
  "dependencies": {
    "firebase-admin": "^11.11.0",
    "firebase-functions": "^4.5.0"
  },
  "devDependencies": {
    "typescript": "^5.0.0",
    "@types/node": "^18.0.0",
    "firebase-functions-test": "^3.1.0"
  },
  "private": true
}
EOL
    print_success "Created package.json"
fi

# Create .env.example
cat > .env.example << EOL
# Firebase project configuration
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_STORAGE_BUCKET=your-project-id.appspot.com
FIREBASE_MESSAGING_SENDER_ID=your-sender-id

# Cloud Functions configuration
FUNCTION_REGION=europe-west1
FUNCTION_MEMORY=256MB
FUNCTION_TIMEOUT=60s

# Development settings
FIREBASE_EMULATOR_HOST=localhost
FIRESTORE_EMULATOR_PORT=8080
FUNCTIONS_EMULATOR_PORT=5001
AUTH_EMULATOR_PORT=9099
EOL
print_success "Created .env.example"

# Create .env if it doesn't exist
if [ ! -f .env ]; then
    cp .env.example .env
    print_success "Created .env from template"
fi

# Create .eslintrc.js
cat > .eslintrc.js << EOL
module.exports = {
  root: true,
  env: {
    es6: true,
    node: true,
  },
  extends: [
    "eslint:recommended",
    "google",
  ],
  rules: {
    quotes: ["error", "double"],
    "max-len": ["error", { "code": 100 }],
  },
};
EOL
print_success "Created .eslintrc.js"

# Create .prettierrc
cat > .prettierrc << EOL
{
  "singleQuote": true,
  "printWidth": 100,
  "tabWidth": 2,
  "trailingComma": "es5",
  "semi": true
}
EOL
print_success "Created .prettierrc"

# Install dependencies
print_status "Installing dependencies..."
npm install

# Install dev dependencies
print_status "Installing development dependencies..."
npm install --save-dev typescript @types/node eslint prettier eslint-config-google eslint-plugin-import @typescript-eslint/eslint-plugin @typescript-eslint/parser firebase-functions-test

# Create tsconfig.json if using TypeScript
cat > tsconfig.json << EOL
{
  "compilerOptions": {
    "module": "commonjs",
    "noImplicitReturns": true,
    "noUnusedLocals": true,
    "outDir": "lib",
    "sourceMap": true,
    "strict": true,
    "target": "es2018",
    "baseUrl": "./src"
  },
  "compileOnSave": true,
  "include": [
    "src"
  ]
}
EOL
print_success "Created TypeScript configuration"

# Create src directory structure
print_status "Creating source directory structure..."
mkdir -p src/handlers
mkdir -p src/utils
mkdir -p src/types
mkdir -p src/config

# Create basic TypeScript files
cat > src/index.ts << EOL
import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import { notificationHandler } from './handlers/notificationHandler';

// Initialize Firebase Admin
admin.initializeApp();

// Export functions
export const sendNotification = functions.https.onCall(notificationHandler);
EOL

cat > src/handlers/notificationHandler.ts << EOL
import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

export const notificationHandler = async (data: any, context: functions.https.CallableContext) => {
  // Verify authentication
  if (!context.auth) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'The function must be called while authenticated.'
    );
  }

  const { recipientToken, notification, data: messageData } = data;

  try {
    const message = {
      token: recipientToken,
      notification: notification,
      data: messageData,
    };

    const response = await admin.messaging().send(message);
    console.log('Successfully sent message:', response);
    return { success: true, messageId: response };
  } catch (error) {
    console.error('Error sending message:', error);
    throw new functions.https.HttpsError('internal', 'Error sending notification');
  }
};
EOL

print_success "Created TypeScript source files"

# Update package.json scripts
node -e "
const fs = require('fs');
const package = require('./package.json');
package.scripts = {
  ...package.scripts,
  'build:watch': 'tsc --watch',
  'lint': 'eslint .',
  'lint:fix': 'eslint . --fix',
  'test:watch': 'jest --watch'
};
fs.writeFileSync('package.json', JSON.stringify(package, null, 2));
"
print_success "Updated package.json scripts"

print_status "Setup complete! Next steps:"
echo "1. Edit .env with your Firebase project configuration"
echo "2. Run 'npm run serve' to start the emulator"
echo "3. Run 'npm run deploy' to deploy to production"

print_success "Firebase Functions setup completed successfully!"
