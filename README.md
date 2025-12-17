<p align=center> <img src=resources/mklego-icon128.png> </p>

# CS-358 Project : MKLEGO 

 <p align=center><img src=resources/readme-screenshots/Full_printer.png width=450> </p>

## Project overview

MKLEGO is a 3D printer that uses LEGO bricks to build any given structure layer by layer. The project is based on mofifying a PRUSA MK3 3D printer by 
- replacing the extruder with a custom nozzle that can pick, place and rotate LEGO bricks,
- changing the base plate to a LEGO compatible plate with place for custom LEGO brick dispensers,
- developing custom software to convert 3D models into LEGO brick instructions.


## Software
### How to run
Access the software repository here: [MKLEGO Software](/frontend/).
Run it by :
- From the `frontend` folder, run the project with 
   
   `./gradlew :desktop-app:run`.
- The application window should open. You can see your recent projects and open them if there are any.

Creating a new project :
- To create a new project, click on File > New File, then select an STL or lxfml file to load and slice a 3D model, or nothing to create a blank project.
- You can go back to the recent projects by clicking on File > Recent Projects.

Editing :
- Once you have opened a project, you can add, remove, or paint LEGO bricks.
- You can see the controls by pressing `H`.
- To paint an existing brick: select the targeted brick, then click on a color in the palette.

Supports :
- The translucid bricks are support bricks. They are added automatically underneath hanging bricks. You can also add some manually by using the gray color (rightmost) in the color palette. You can hide/show them by clicking on "Hide Supports" in the top right.

Printing :
- To export your LEGO project: open the desired project and go to File > Export as lgcode.
- To print your LEGO structure, export it as lgcode, store it in a SD card and transfer the file to the printer. Then, you can set the printer and launch the build through the printer's screen interface.
- You will be asked to show on the screen where your dispensers are placed and then the real printing will commence!

Screen Interface:
- Home Screen: you can select to either go to print or to settings
- Settings: Silent mode: If you do not want to hear the screen beep, Calibrate: Calibrate the x, y, z axis and the rotation (please do it before printing anything), Back: Get back to main menu (Home)
- Print menu: Press Files to chose a file to execute and Back to get back to Home. If on files chose which file to execute.
- File execution: If .TXT file the screen will play a music corresponding to the file. If .LG or .LGCODE file the model will be printed


### Pipeline
The JavaFX-based software pipeline includes:
- Loading a 3D model from an STL or lxfml (v5.0 Legacy LDD compatible) file,
- Slicing it into LEGO brick layers and assembling them in an optimal agencement,
- Rendering the LEGO structure in a 3D viewer to view and edit the build if needed,
- Generating the lgcode (custom g-code) instructions to be sent to the printer.


## Hardware architecture
 <p align=center><img src=resources/readme-screenshots/schematic-overview1.png> </p>
 <p align=center><img src=resources/readme-screenshots/schematic-overview2.png> </p>
 <p align=center><img src=resources/readme-screenshots/schematic-overview3.png> </p>
 More details in the [Hardware Documentation](/resources/Mklego_Diagram.pdf).

### Firmware
Access the firmware repository here: [MKLEGO Firmware](/embedded/mklego).

### System Overview

The hardware is organized around a **dual-microcontroller architecture**:

- **Main Controller**: STM32 Nucleo-64 (NUCLEO-F411RE)  
  Responsible for motion control (stepper motors), safety-critical timing, and real-time coordination.

- **Baseplate Controller**: ESP32-S3-DevKitC  
  Responsible for high-level logic, sensor acquisition, dispenser module control, and communication with the main controller.

The system integrates:
- Up to **8 LEGO dispenser modules**
- **3-axis stepper motor control (X, Y, Z)**
- A **servo motor** that pushes the brick down through the nozzle
- A centralized **power supply unit (PSU)** providing 5 V and 12 V rails


# Building tutorial

## Step 1: Preparation
This prototype require a Prusa Mk3 printer as a base.   
Remove from the Prusa the heat bed, the nozzle and the PSU. They won't be used for this project.

## Step 2: Assembling the components


---
<p align=center><img src=resources/readme-screenshots/Nozzle.png width=450> </p>
<p align=center><img src=resources/readme-screenshots/NozzlePhoto.png width=450> </p>

### Nozzle

---
#### Material:
- [ ] 1 Nema 14 stepper motor
- [ ] 1 servo motor 25kg/cm
- [ ] 2 brass hollow cylinder of external diameter 8mm and internal diameter 5mm and 64mm long
- [ ] 2 metal rods 97mm long
- [ ] 3 springs of 10 mm length
- [ ] 1 micro-switch
- [ ] M3 and M2.5 nuts and screws
- [ ] MDF and PETG for laser cutted and 3D printed pieces

#### Process:
All the MDF to print and the plastic parts to print are available as DXF and STL files.   
There is also a 3MF files with the 3D model of the nozzle assembled.   
To smoothly clip all the parts together:
- First assemble the parts sliding on the metal rods.
- Then make the rotating parts with the servo.
- Finally complete the nozzle with the not-rotating part.


### Notes
Primarily use M3 screws. M2.5 depending on the 3d files


---
<p align=center><img src=resources/readme-screenshots/BasePlate.png width=450> </p>

### BasePlate

---
#### Material:
- [ ] 1 micro-switch
- [ ] MDF and PETG for laser cutted and 3D printed pieces
- [ ] 1 22 by 22 stubs Lego plate

#### Process:
All the MDF to print and the plastic parts to print are available as DXF and STL files.   
There is also a 3MF files with the 3D model of the baseplate assembled.   
Assemble everything together (exept the lego plate).

### Notes:
Use M3 conical screws to connect the baseplate to the printer

---
<p align=center><img src=resources/readme-screenshots/Dispensers.png width=450> </p>

### Dispensers

---
#### Materials (per dispenser):
- [ ] 1 IR Leds
- [ ] 1 IR Receiver
- [ ] M3 nuts and screws
- [ ] MDF and PETG for laser cut and 3D printed pieces

#### Process:
All the MDF to print and the plastic parts to print are available as DXF and STL files.   
There is also a 3MF files with the 3D model of the dispensers assembled.  
Assemble the ramp then add the led and the receiver.

### Notes:
Use M3 conical screws to connect the dispensers as there needs to not be anything between them
do not add any screws to the middle screw holes of the dispensers
The path to lay the wires on the left side of the dispenser needs to be filed down to fit two wires 

---
<p align=center><img src=resources/readme-screenshots/ElectricalBox1.png width=450> </p>

### Electrical Box 1

---
### Materials
- [] 1 Arduino Mega
- [] 1 CNC shield with 4 motor drivers
- [] 1 motor driver
- [] 2 buck converters

### Setup instructions:
Arduino mega connects to the four motor drivers on the CNC shield
The four mk3 mothors are connected to the CNC shield
The motor driver is driving the Nema 14
Micro switches are connected to the Arduino Mega
Arduino controlls the servo motor (PWM pin)
Buck converter at 7V powers the Servo motor 
Buck converter at 8V powers the Nema 14
Barrel jack for the power supply at 12V powering the CNC shield and the buck converters
Arduino mega commmunicates with the ESP32(second electrical box) via a custom protocol found in the arduino code on the github

---
<p align=center><img src=resources/readme-screenshots/ElectricalBox2.png width=450> </p>

### Electrical Box 2

---
### Materials
- [] 1 ESP32 S3 Dev Module
- [] 1 MOSFET 

### Setup instructions:
find the dxf files at [link]
TODO
TODO
TODO
TODO
TODO
TODO
TODO
TODO
TODO
TODO
TODO
TODO
TODO
TODO
TODO
TODO
TODO
TODO

### Notes:
This electrcial box connects to the dispensers
Connected by velcro to the printer body
The esp32 is connected to the dispensers and reads an adc input inside of a Voltage divider. Depending on the adc input we can deduce if a dispenser is connected, and which one it is, based on its internal resistance.
