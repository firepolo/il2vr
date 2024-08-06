# Il2 VR mod

## Installation
Add in file **config.ini**
```ini
[VR]
factor=0.5
```
**factor** is a real for control the resolution of game by the VR headset resolution.
For Oculus Rift S, the recommended resolution is 2016x2172 but a big resolution impact the performance of game. With the *factor = 0.5* the game resolution is $(2016*0.5)$x$(2172*0.5)=1008$x$1086$. 0.5 is the default value but you can change for improve the resolution.

## Todo
- Fix artefacts when upscale low resolution to VR resolution with function *glBlitFramebuffer*. Use multisample works but don't allow different size between source and destination. I have to fix that with alternative.
- Fix gimballock with headset rotation only *Yaw* axis don't work.
- Fix for better headset movements
