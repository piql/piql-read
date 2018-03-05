/*****************************************************************************
**
**  Implementation of the bitutils interface
**
**  Creation date:  2016/06/28
**  Created by:     Piql
**
**
**  Copyright (c) 2016 Piql AS. All rights reserved.
**
**  This file is part of the boxingdata library
**
*****************************************************************************/

//  PROJECT INCLUDES
//
#include "boxing/math/bitutils.h"

uint64_t htobell(uint64_t hostlonglong);
uint32_t htobel(uint32_t hostlong);
uint16_t htobes(uint16_t hostshort);
uint64_t betohll(uint64_t belonglong);
uint32_t betohl(uint32_t belong);
uint16_t betohs(uint16_t beshort);
