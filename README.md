# APKPatcher
Requirements of running the tool is to have Android SDK, APK Tool and a JDK Installation, which are pretty common, if you're into this kind of stuff!

The setup is quite simple, when you run the released jar from command line, the program looks for ANDROID_HOME environment variable, JVM parameter and the configuration file, created after the first run to see if its been set, and if it hasn't it asks user to input it.

ANDROID_HOME here is the root folder of Android SDK, which could automatically be detected to some extent, but as I have other things to do.. for now.. It just looks for the environment variable and command line flag.. You can see where Android SDK is located from Android studio, but at least in Windows environment its usually located in 

    C:\Users\PasiMatalamaki\AppData\Local\Android\Sdk
Where PasiMatalamaki is replaced with your epic us3rname.

Aftere setting up ANDROID_HOME, we're not done yet.. The next thing to set is path to APK Tool.jar which can be downloaded from here. Just place it anywhere and copy the path to the jar and paste it in.. after that is the final step, before getting to the real deal! The most important part, which hopefully you've already set! Setting JAVA_HOME environment variable or just inputting it..

As written in the first Google result, If you didn't change the path during installation, it will be something like this: C:\Program Files\Java\jdk1.8.0_65

And that is the final step before getting into the tool, after that you will be asked for build tools version you'd like to use, if you've got multiple.

All should work, but I am myself using the newest one, this is other thing, that could be modified to automatically pick the newest one, but man.. the effort!

    Choose your buildToolsDir
    0: 19.1.0
    1: 21.1.2
    2: 23.0.1
    3: 23.0.3
    4: 24.0.0
    5: 24.0.1
  
    
So I input 5 and I'll be asked for the apk to be used, or if the folder you're working in, as in the folder you ran the jar in, contains none, you will be asked for a directory where the apk is, or just a full path to the apk.

Choose APK to be used
0: com.nianticlabs.pokemongo(version: 0.35.0, name: pokemon_original.apk)
For example here's only one apk, which is the pokemon go, as used in this example, so we select 0.

After that the application will list available patches in the given directory, which by default is the /patches folder, which by default will look like this

    Toggle selected patches, or press enter to proceed patching..
    0: [ ] frida_gadget
    Add Frida Gadget

    Adds Frida Gadget to libs and loads it, easy!
    1: [ ] remove_pinning
    Remove Certificate Pinning
    
These are the two directories inside the /patches folder, which contains the following structure:

    * patch_name:
      * details.txt
      * replaced files...
as example there two patches in the release. The patching simply replaces the files inside the apk by the files inside the patch, simple and efficient!

You can toggle the patches by their version number, for example, here we want to apply remove pinning patch, we just input 1, and after that we can press enter to proceed with patching.

After that we'll see a long output of information of whats happening, but after its all done, we'll see output file

build_output_release.apk
which is result of debuilding, patching, rebuilding, signing and aligning, which is ready to be installed on an Android device! Simply run 

    adb install build_output_release.apk

And enjoy your Pokemon Go without certificate pinning!
