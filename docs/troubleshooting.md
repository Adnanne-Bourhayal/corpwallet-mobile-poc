# Troubleshooting

**The queue says review unavailable.** On the computer, open `http://127.0.0.1:8000/health`. Run `adb devices`, select your device and restore the ADB reverse mapping. The sample needs no Wi-Fi IP address.

**A release asks for an API address.** Set the Gradle property `walletApiUrl` to your own HTTPS API origin when building. Loopback HTTP is a debug-only convenience.

**A decision already exists.** Refresh the desk to read the server's current status and reason. An automatically declined wallet cannot be approved. To restore all fictional fixtures, post to `/demo/reset` on your own running API.

**A report lists two unknown settings.** That is expected. The native export declares the debugging and obfuscation settings only. An unknown field is also expected when its values disagree.
