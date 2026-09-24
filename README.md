# BMD Curtain Remote

Android TCP remote for the BMD Haulage curtain relay.

## Commands

- Open ON: `A0 01 01 A2`
- Open OFF: `A0 01 00 A1`
- Close ON: `A0 02 01 A3`
- Close OFF: `A0 02 00 A2`

The app sends the bytes as a single TCP connection to the configured IP and port.

Default port: **8080**

## Build

GitHub Actions is configured to build a debug APK automatically. The APK will appear under the workflow run's Artifacts as `bmd-curtain-debug-apk`.

## Important

The IP address is a placeholder and should be changed to the IP address of the relay module on the trailer network.
