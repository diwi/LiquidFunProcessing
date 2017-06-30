![LiquidFunProcessing](https://github.com/diwi/LiquidFunProcessing/blob/master/screenshots/examples/diewald_LiquidFunProcessing_Chains.jpg)


# LiquidFunProcessing
**Box2D/LiquidFun Library for Processing.**

Particle based fluid simulation and rigid body simulation using [jBox2D/LiquidFun 2.3.0+](https://github.com/jbox2d/jbox2d).


JavaDoc: http://thomasdiewald.com/processing/libraries/liquidfun/reference/index.html

<br>

## Download
+ [Releases](https://github.com/diwi/LiquidFunProcessing/releases)
+ [LiquidFunProcessing Website](http://thomasdiewald.com/processing/libraries/liquidfun)
+ Processing IDE -> Library Manager

<br>

![LiquidFunProcessing](https://github.com/diwi/LiquidFunProcessing/blob/master/screenshots/examples/diewald_LiquidFunProcessing_Examples2.jpg)

## Examples
The library includes a lot of examples from the original [jbox2d testbed](https://github.com/jbox2d/jbox2d/tree/master/jbox2d-testbed/src/main/java/org/jbox2d/testbed/tests).<br>
For a quick start, each sketch has a couple of default eventhandlers:

- LMB: drag rigid bodies / particles
- LMB + SHIFT: shoot a bullet
- MMB: spawn particles
- RMB: destroy particles

... as well as some key-mapping, e.g. to display the debug-draw, restart, pause/resume.<br>
Of course, each of those can be altered, overwritten or disabled/removed.

<br>


## Rendermodes
Default renderer, no postprocessing, particles are rendered as point-sprites, rigid bodies are rendered using the PShape tree structure.
![Default](https://github.com/diwi/LiquidFunProcessing/blob/master/screenshots/rendermodes/diewald_LiquidFunProcessing_RenderRaw.jpg)

Default renderer + Postprocessing to get a liquid effect and some fake shading.
PixelFlow (https://github.com/diwi/PixelFlow) needs to be installed to use it.
![LiquidFx](https://github.com/diwi/LiquidFunProcessing/blob/master/screenshots/rendermodes/diewald_LiquidFunProcessing_RenderLiquidFx.jpg)

Debug Draw, using the box2d world for rendering.
![Debug](https://github.com/diwi/LiquidFunProcessing/blob/master/screenshots/rendermodes/diewald_LiquidFunProcessing_RenderDebug.jpg)

## Resources

- jBox2d web: http://www.jbox2d.org/
- jBox2d Testbed: https://github.com/jbox2d/jbox2d/tree/master/jbox2d-testbed
- Box2d Testbed: https://github.com/erincatto/Box2D/tree/master/Box2D/Testbed
- LiquidFun: http://google.github.io/liquidfun/
- **LiquidFun Programmers Guide: http://google.github.io/liquidfun/Programmers-Guide/html/index.html**

<br>

## Installation, Processing IDE

- Download [Processing 3](https://processing.org/download/?processing)
- Install liquidFunProcessing via the Library Manager.
- Or manually, unzip and put the extracted PixelFlow folder into the libraries folder of your Processing sketches. Reference and examples are included in the PixelFlow folder. 

- Also make sure you have the latest graphics card driver installed!

#### Platforms
Windows, Linux, MacOSX


<br>

## Dependencies, to run the examples

 - **PixelFlow: https://github.com/diwi/PixelFlow**

