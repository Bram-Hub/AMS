# AMS
## Authors
2006:
Stan Bak  
2010:
Matthew Turnbull  
2012:
Martin Papesh  
2013:
Tim Treese
2015:
Vatsal Bhagwani

## About
---VERSION 3.1 RELEASE NOTES--------------------------------------
IMPROVEMENTS OVER 2.2:

1) Modify icon changed to match TM software

2) Icon order changed around with modify icon as default selection

3) Added button to reset registers. Clears all registers and resets machine back to starting state

4) Added new speeds (slow, fast, very fast, and compute) similar to TM software for easier simulation

5) Created step button that allows user to progress through the simulation one step at a time

6) Multiple windows can now be opened and closed. Previously closing one window would close the entire program

7) Modified edit state window to only allow for changing the register number. Other options are commented out in code should they need to be brought back

8) Reset & clear registers buttons will not start the machine running again immediately, as was the case in 2.2 when the system was still locked

9) Scaling the window now correctly adjusts the register locations so they can be accurately clicked on. Additionally, when using the arrows to go left and right in the 
	registers, the registers now jump as opposed to the gliding motion. This was done for consistency of clicking on the points
	

BUGS:

1) Users have reported the starting node switching when opening a saved file. This bug was unable to be reproduced, but should be marked as active

2) Converting to the turing machine, first register starts at 1 when it should start at 0 



---VERSION 2.2 RELEASE NOTES--------------------------------------

IMPROVEMENTS OVER 2.1:

1) Middle-mouse click-and-drag to scan the canvas now works with left-mouse (more intuitive and works on mice with no middle button).
   To move nodes using the "Modify" tool, select them once (they should have a red outline) and then drag them.

2) In 2.1, in some cases, when the close dialog is brought up (either by clicking the upper-right "x" or File>Exit), when "cancel" is selected, the program still exits.
   As a temporary fix (as this could cause massive unexpected data loss), the "cancel" option has been removed to protect the user. The previous code has been commented out to allow for easy reworking if the bug can be identified.

3) Specific buttons to increment and decrement each register have been added for much quicker initial register-setting (when numbers are small).

4) An error was fixed in which the save file option would save empty files on newer versions of Java. This fix is due to Michael Usher (usherm@rpi.edu).

KNOWN BUGS:

1) As part of the temporary solution described in improvement 2, there is currently no "cancel" option in the close dialog. This is not ideal.

2) In some cases, clicks on the register panel do not register correctly. When this error is present, the program detects the clicks as appearing far further left than they are (i.e. clicking on register 2 activates the register 1 dialog).
   The source of the error is not known. It appears as though the register panel class is fed a bad point. It appears that on some machines, this error never manifests, and on other machines, it manifests all the time. It could be a Java version issue. More research is needed


---VERSION 2.1 RELEASE NOTES--------------------------------------
AMS is an abacus machine simulator program. You can create, edit, save, and then later load your abacus machine. Simulating features are also provided.
There is a help menu that I recommend you read, since many action have short cuts that may not appear obvious. For example you can drag around the work area by dragging with the middle mouse button, or zoom by scrolling with the mouse. 
This program is distributed under the GNU GPL, which pretty much means that 1) it might not work and 2) if you redistribute it, make sure you provide the source. For a more formal statement see LISENSE.txt.



Software requirements: JDK1.5+

To use the run and build scripts ("ams" and "ams_build" in Linux - "ams.bat" and "ams_build.bat" in Windows) the JRE/JDK needs to be configured in your operating system's PATH environment variable.



Owen Turing Machine Simulator exporter information:
Machines can be exported to the Owen TMS via [ File -> Export OwenTMS ... ]. The Turing Machine is constructed in the following manor:

1. Moves the head to the "0" directly following the desired block.
2. Performs the desired action:
   - Addition adds one to the block and shifts the following blocks 1 to the right
   - Subtraction removes one from the block (if possible) and shifts following blocks one to the left (if needed).
3. Moves the head to the beginning of the tape in preparation for the next operation.

All of the values on the tape are represented in the 1+n encoding. So a 0 is "1" and a 3 is "1111". All value blocks are separated by a single "0". So the input "4 5 0" is represented as "11111011111101".

There is a caveat, in which the Turing Machine will not clean up the tape after computation (beyond what is done by the Abacus Machine). Any 'temporary' registers used by the Abacus Machine will show on the resulting tape.

For example, if the Abacus Machine's computation results in:
- the argument (i.e. "3") in register 0
- the computed value (i.e. "2") in register 1
- registers 2, 3, and 5 were used during computation (and set them all back to 0 before finishing)
the resulting tape would be "...1111011101010101..."
