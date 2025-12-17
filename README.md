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


### Pipeline
The JavaFX-based software pipeline includes:
- Loading a 3D model from an STL or lxfml (v5.0 Legacy LDD compatible) file,
- Slicing it into LEGO brick layers and assembling them in an optimal agencement,
- Rendering the LEGO structure in a 3D viewer to view and edit the build if needed,
- Generating the lgcode (custom g-code) instructions to be sent to the printer.


## Hardware architecture
 <p align=center><img src=resources/readme-screenshots/schematic-overview.png> </p>
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
- Up to **6 LEGO dispenser modules**
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

### Nozzle

---
#### Material:
- [ ] 1 Nema 14 stepper motor
- [ ] 1 servo motor 15kg/mm
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

---
<p align=center><img src=resources/readme-screenshots/Dispensers.png width=450> </p>

### Dispensers

---
#### Materials (per dispenser):
- [ ] 1 IR Leds
- [ ] 1 IR Receiver
- [ ] M3 nuts and screws
- [ ] MDF and PETG for laser cutted and 3D printed pieces

#### Process:
All the MDF to print and the plastic parts to print are available as DXF and STL files.   
There is also a 3MF files with the 3D model of the dispensers assembled.  
Assemble the ramp then add the led and the receiver.




