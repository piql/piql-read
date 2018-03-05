/*****************************************************************************
**
**  Definition of platform spesific functions
**
**  Creation date:  2016/10/14
**  Created by:     Ole Liabo
**
**
**  Copyright (c) 2016 Piql AS. All rights reserved.
**
**  This file is part of the unboxing library
**
*****************************************************************************/

//  PROJECT INCLUDES
//
#include "boxing/platform/platform.h"

/****************************************************************************/
/*! \defgroup platform Platform dependent functions and structures
 *  \ingroup unbox
 *
 *  The platform is defined as the computing environment responsible for 
 *  executing the unboxing software and providing the resources the 
 *  unboxing software needs. This includes reading the frames from the film
 *  reel and converting them into 8bit 2D images, and software library 
 *  functions like dynamic memory allocation, math, floating point and other 
 *  support functions. All platform dependant function used directly by 
 *  the unboxing library is part of the C99 version of the C programming
 *  language. All the C language routines used are documented here. The 
 *  C99 function documentation are from the GNU Linux man pages 
 *  project (C).
 */


//----------------------------------------------------------------------------
/*! \ingroup platform
 *  \fn  void *malloc(size_t size)
 *  \brief Allocate dynamic memory
 *
 *  malloc() allocates size bytes and returns a pointer to the allocated memory.  
 *  The memory is not cleared.  If size is 0, then malloc() returns either NULL,  
 *  or  a  unique  pointer value that can later be successfully passed to free().
 *
 *  \param  size   Size of memory buffer to allocate in bytes.
 *  \return NULL or unique pointer
 */

//----------------------------------------------------------------------------
/*! \ingroup platform
 *  \fn  void free(void *ptr)
 *  \brief Free dynamic memory
 *
 *  free() frees the memory space pointed to by ptr, which must have been 
 *  returned by a previous call to malloc().  Otherwise, or if free(ptr) has 
 *  already been called before, undefined behavior occurs. If ptr is NULL, no 
 *  operation is performed.
 *
 *  \param  ptr   Unique pointer to free.
 */

//----------------------------------------------------------------------------
/*! \ingroup platform
 *  \fn  void *memset(void *s, int c, size_t n)
 *  \brief Fill memory with a constant byte
 *
 *  The  memset() function fills the first n bytes of the memory area pointed 
 *  to by s with the constant byte c.
 *
 *  \param  s   Pointer to memory area.
 *  \param  c   Constant byte 
 *  \param  n   Number of bytes to set.
 *  \return A pointer to the memory area s.
 */

//----------------------------------------------------------------------------
/*! \ingroup platform
 *  \fn void *memcpy(void *dest, const void *src, size_t n)
 *  \brief Copy memory area
 *
 *  The memcpy() function copies n bytes from memory area src to memory area 
 *  dest. The memory areas should not overlap. 
 *
 *  \param  dest   Pointer to destination memory area.
 *  \param  src    Pointer to source memory area. 
 *  \param  n      Number of bytes to copy.
 *  \return A pointer to dest.
 */

//----------------------------------------------------------------------------
/*! \ingroup platform
 *  \fn void *alloca(size_t size)
 *  \brief Allocate memory that is automatically freed
 *
 *  The alloca() function allocates size bytes of space in the stack frame of 
 *  the caller. This temporary space is automatically freed when the function 
 *  that called alloca() returns to its caller. 
 *
 *  \param  size   Size of memory buffer to allocate in bytes.
 *  \return NULL or unique pointer
 */
