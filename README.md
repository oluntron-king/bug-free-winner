# MYSTIC — Android Kotlin + Supabase

MYSTIC is an Android-only social app starter built with Kotlin and Jetpack Compose. The visual style is dark navy with electric-blue accents.

## Open in GitHub Codespaces
1. Open this repository on GitHub.
2. Click **Code → Codespaces → Create codespace on main**.
3. In the Codespaces terminal, follow the build notes below. The starter does not include a Gradle wrapper yet, so the environment must have Gradle available or the wrapper should be generated.

## Configure Supabase
1. Create a project at https://supabase.com/dashboard.
2. Copy the Project URL and the publishable key (or legacy anon key).
3. Copy `local.properties.example` to `local.properties` and replace the placeholders.
4. Run `supabase/mystic_schema.sql` in the Supabase SQL Editor.

Never put a Supabase secret/service_role key in an Android client.

## Build
Install JDK 17 and Android SDK Platform 36, then run:
```bash
gradle assembleDebug
```
Expected APK: `app/build/outputs/apk/debug/app-debug.apk`

## Current scope
Includes the initial Compose UI, email/password Supabase authentication, and a read-only posts feed. Post creation, image upload, likes, profiles, and real-time chat are not implemented yet.
