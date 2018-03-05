#ifndef SYNCPOINTS_H
#define SYNCPOINTS_H

/*****************************************************************************
**
**  Definition of the synchronization points correction interface
**
**  Creation date:  2014/12/16
**  Created by:     Piql AS
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

#include "boxing/frame/frametrackercbgpf_1.h"

boxing_matrixf* boxing_syncpoints_correct_frame_geometry(
    void* user, const boxing_image8 * frame, boxing_matrixf *symbol_location_matrix,
    const boxing_matrixi *sync_point_location_index,
    const correct_frame_geometry_properties * properties);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif // SYNCPOINTS_H
