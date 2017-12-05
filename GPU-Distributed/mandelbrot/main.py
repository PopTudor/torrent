#!/usr/bin/env python
# -*- coding: utf-8 -*-

from __future__ import absolute_import, print_function
import numpy as np
import pyopencl as cl
import scipy.misc
from PIL import Image
import sys
import os
os.environ['PYOPENCL_COMPILER_OUTPUT'] = '1'
WIDTH = 12
HEIGHT = 12
NO_PIXELS = 3

platforms = cl.get_platforms()
devices_m = platforms[0].get_devices()
print ('Devices:\n')
for it in devices_m:
  print (it)
gpu = platforms[0].get_devices(device_type=cl.device_type.GPU)
gpu.pop(0)
print(gpu)

ctx = cl.Context(devices = gpu)
queue = cl.CommandQueue(ctx)

mf = cl.mem_flags
bufferSize = WIDTH*HEIGHT*NO_PIXELS
host_image = np.zeros(shape=(bufferSize), dtype=int, order='C')

image = cl.Buffer(ctx, mf.WRITE_ONLY|mf.COPY_HOST_PTR, hostbuf=host_image)

with open("./generate.cl", "r") as kernel_file:
  kernel = "".join(kernel_file.readlines()[1:])

program = cl.Program(ctx, kernel).build()
program.generate_image(queue, host_image.shape, None, image)

res_np = np.empty_like(host_image)
cl.enqueue_copy(queue, res_np,image)
Image.fromarray(host_image.astype('uint8')).save('./outfile.jpg')

# Check on CPU with Numpy:
print(res_np)
