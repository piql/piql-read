#ifndef STRINGC_H
#define STRINGC_H

/*****************************************************************************
**
**  Definition of the string interface
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
#include <stddef.h>
#include "gvector.h"
#include "boxing/bool.h"

char *    boxing_string_clone(const char * string);
char *    boxing_string_allocate(size_t string_length);
void      boxing_string_free(char * string);
void      boxing_string_copy(char * destination, const char * source);
gvector * boxing_string_split(const char * string, const char * separator);
DBOOL     boxing_string_equal(const char *string_1, const char *string_2);
DBOOL     boxing_string_to_integer(int* value, const char *string);
size_t    boxing_string_length(const char *s);
void      boxing_string_trim(char** string);
void      boxing_string_cut(char** string, size_t start_index, size_t end_index);

int 	strcmp (const char * s1, const char * s2)
size_t 	strlen(const char *s)
char 	*strchr(const char *s, int c)
void 	*memset(void *s, int c, size_t n)
void 	*memcpy(void *dest, const void *src, size_t n)
int		 memcmp(const void* s1, const void* s2,size_t n)
void 	*memmove(void *dest, const void *src, size_t n)

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif // STRINGC_H
