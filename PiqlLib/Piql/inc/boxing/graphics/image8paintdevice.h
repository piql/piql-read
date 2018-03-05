#ifndef DIMAGE8PAINTDEVICEC_H
#define DIMAGE8PAINTDEVICEC_H

/*****************************************************************************
**
**  Definition of the image8 paint device interface
**
**  Creation date:  2014/12/16
**  Created by:     Haakon Larsson
**
**
**  Copyright (c) 2014 Piql AS. All rights reserved.
**
**  This file is part of the unboxing library
**
*****************************************************************************/

#ifdef __cplusplus
extern "C" {
#endif

//  PROJECT INCLUDES
//
#include "boxing/graphics/paintdevice.h"
#include "boxing/image8.h"



//============================================================================
//  CLASS:  boxing_image8paintdevice

typedef struct boxing_image8paintdevice_s
{
    boxing_paintdevice base;
    boxing_image8      image;

} boxing_image8paintdevice;

boxing_paintdevice * boxing_image8paintdevice_create(boxing_image8 * image);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* DIMAGE8PAINTDEVICEC_H */
