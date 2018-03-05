#ifndef DATAPOINTSC_H
#define DATAPOINTSC_H

/*****************************************************************************
**
**  Definition of the data points interface
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

#include "horizontalmeasures.h"
#include "boxing/image8.h"
#include "gvector.h"

gvector * boxing_datapoints_quantize(const boxing_image8 * image, int block_width, int block_height, int levels);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif // DATAPOINTSC_H
