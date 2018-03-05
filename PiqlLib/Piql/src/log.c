/*****************************************************************************
**
**  Implementation of the logging interface
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
#include "boxing/log.h"

#if !defined (DLOG_DISABLED)
//  PRIVATE INTERFACE
//

static void def_boxing_fatal_exception(const char * str);

// PUBLIC LOG FUNCTIONS
//

boxing_fatal_exception_cb_t boxing_fatal_exception = &def_boxing_fatal_exception;

// PRIVATE LOG FUNCTIONS
//

static void def_boxing_fatal_exception(const char * str)
{
    fprintf(stderr, "%s", str);
#ifdef _DEBUG
    abort();
#else
    exit(1);
#endif // _DEBUG
}

#endif // DLOG_DISABLED
