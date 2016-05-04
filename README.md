# Fx2C
Fxml 2 Compiler - Speedups JavaFx Applications by compiling Fxml Resources

It does by replacing Fxml files to their Java code.

This compiler would reduce cold start of your JavaFx applications and will make your
dialogs to be more responsive by removing the JavaFX FXML parsing and reflection "tax".

You should expect at least 30% cold warmup speedup (from 80 ms to 55-60 ms on a powerful
laptop) when you start on a higher spec Java for JavaFX "Hello world sample". If using
a preloader (see the last section), the code wil run instantly (less than 1 ms to
create a full FXML "Hello world" application, a bit less than 5 ms on an Atom based CPU).

For example (these numbers are just for reference), using Excelsior JET's
precompilation will make the start time of just 30 ms (without preloader) for a simple
FXML application. The difference could be wider for more complex UIs but it was
not fully tested.

# How to use it

- Clone this project (or download it as a zip from GitHub)

- Open it by folder with your IDE of choice (Netbeans, Eclipse, IDEA)

- Edit MainApplication.run() to point to your location where you have FXMLs in the root of your project.

# Expected results:

- every myFile.fxml will have at the same location a FxMyFile.java

- instead of using FxmlLoader ... you can use FxMyFile fxMyFile = new FxMyFile(); to construct the same UI

- fxMyFile._view will return your root FXML2 control

- if you set a controller, fxMyFile._controller will return your Controller instance.

#Preloader

- in the root folder you should have a Preloader class, which is named by the last folder name + "Fx2CPreloader.java",
something like: "Java3DFx2CPreloader.java"

- create a preloader if you have a loading splash screen or similar logic: new JavaFx3DFx2CPreloader();

- the preloader helps Java VM to locate most of your UI JavaFX views and controllers, making the second instantiation
of your future "new FxMyFile()" very quick.

Please report any bugs and inconsistencies.
