# AMS
## Authors
2006:
Stan Bak  
2010:
Matthew Turnbull  
2012:
Martin Papesh

## About
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
