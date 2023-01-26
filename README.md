# Using RealWear's Barcode Reader in a Web App

This sample project shows you how to use RealWear's Barcode Reader in an Android
web app, receiving the scan result in JavaScript.

## Setting it up

### Running the project

1. You require node.js version 16 or above or running the Vue app fails.
    - To install it on Windows:
        1. Install nvm via this link https://github.com/coreybutler/nvm-windows/releases
        2. In Command Prompt, use `nvm ls`. You should have an asterisk next to a node version that is 16 or higher.
2. `cd vue-project`
3. `npm install`
4. `npm run build`
5. Open this folder in Android Studio and proceed as normal to build and run on your device.

### Hot reloading

This sample project has hot reloading setup. When you set it up and run `npm run dev-android`, any changes to files
in Vue will cause the app to re-build and deploy instantly onto your chosen device.

To setup hot reloading:

1. Ensure you've ran the above steps for building the web app, to confirm you have installed the node.js dependency.
2. Your system environment needs to have Java 11 (64-bit) setup, with a path to it linked to env variable 'JAVA_HOME'.
    1. For convenience, we use the Java distribution configured with Android Studio. You can find the path to this in
    `File > Project Structure > SDK Location > Gradle Settings` and reading the path set on `Gradle JDK`. Usually, the
    path for Windows is `C:/Program Files/Android/Android Studio/jre`.
    2. In Command Prompt, run `set JAVA_HOME="C:/Program Files/Android/Android Studio/jre"` (replace with your directory
    if different) each time you want to run hot reloading.
        - If using PowerShell, instead run `$env:JAVA_HOME = "C:/Program Files/Android/Android Studio/jre"`.
        - If using Bash, instead run `export JAVA_HOME="C:/Program Files/Android/Android Studio/jre"`.
        - For ease of use, consider setting the env variable permanently in your system environment variables
        (if you choose this option, remember to close and re-open your command prompt).
2. `cd vue-project`
3. `npm install`
4. `npm run dev-android`
