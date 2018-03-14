cmake_minimum_required(VERSION 3.0.0)

################### Variables. ####################
# Change if you want modify path or other values. #
###################################################

set(PROJECT_NAME unboxingdata)

# Output Variables

set(OUTPUT_DEBUG debug/bin)
set(OUTPUT_RELEASE release/bin)

# Def

add_definitions(-DBOXING_USE_C99_LIBRARIES)

# Folders files

set(CPP_DIR_1 Piql/src)
set(CPP_DIR_2 Piql/src/frame)
set(CPP_DIR_3 Piql/src/math)
set(CPP_DIR_4 Piql/src/graphics)
set(CPP_DIR_5 Piql/src/codecs)
set(CPP_DIR_6 Piql/src/unboxer)
set(CPP_DIR_7 Piql/src/base)
set(CPP_DIR_8 Piql/src/platform)
set(CPP_DIR_9 Piql/thirdparty)
set(CPP_DIR_10 Piql/thirdparty/ldpc)
set(CPP_DIR_11 Piql/thirdparty/bch)
set(CPP_DIR_12 Piql/thirdparty/glib)
set(CPP_DIR_13 Piql/thirdparty/reedsolomon)
set(HEADER_DIR_1 Piql/inc/boxing/codecs)
set(HEADER_DIR_2 Piql/src/frame)
set(HEADER_DIR_3 Piql/inc/boxing/unboxer)
set(HEADER_DIR_4 Piql/thirdparty/ldpc)
set(HEADER_DIR_5 Piql/thirdparty/bch)
set(HEADER_DIR_6 Piql/inc/boxing/math)
set(HEADER_DIR_7 Piql/inc/boxing)
set(HEADER_DIR_8 Piql/inc/boxing/graphics)
set(HEADER_DIR_9 Piql/src/unboxer)
set(HEADER_DIR_10 Piql/inc)
set(HEADER_DIR_11 Piql/inc/boxing/frame)
set(HEADER_DIR_12 Piql/inc/boxing/platform)
set(HEADER_DIR_13 Piql/inc/boxing/platform.h)
set(HEADER_DIR_14 Piql/inc/boxing/types.h)
set(HEADER_DIR_15 Piql/inc/boxing/memory.h)

############## CMake Project ################
#        The main options of project        #
#############################################

project(${PROJECT_NAME} C)

# Define Release by default.
if(NOT CMAKE_BUILD_TYPE)
  set(CMAKE_BUILD_TYPE "Release")
  message(STATUS "Build type not specified: Use Release by default.")
endif(NOT CMAKE_BUILD_TYPE)

# Definition of Macros
add_definitions(
   -D_LARGEFILE_SOURCE 
   -D_FILE_OFFSET_BITS=64 
   -DBOOST_SYSTEM_NO_DEPRECATED 
   -D_WINDOWS 
   -DUNICODE 
   -DWIN64 
   -DRAND_FILE="./randfile" 
   -DNDEBUG 
)

############## Artefacts Output #################
# Defines outputs , depending Debug or Release. #
#################################################

if(CMAKE_BUILD_TYPE STREQUAL "Debug")
  set(CMAKE_LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/${OUTPUT_DEBUG}")
  set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/${OUTPUT_DEBUG}")
  set(CMAKE_EXECUTABLE_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/${OUTPUT_DEBUG}")
else()
  set(CMAKE_LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/${OUTPUT_RELEASE}")
  set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/${OUTPUT_RELEASE}")
  set(CMAKE_EXECUTABLE_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/${OUTPUT_RELEASE}")
endif()

################# Flags ################
# Defines Flags for Windows and Linux. #
########################################

if(MSVC)
   set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} /W3 /EHsc")
   set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} /W3 /EHsc")
endif(MSVC)
if(NOT MSVC)
   set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
   if ("${CMAKE_CXX_COMPILER_ID}" STREQUAL "Clang")
       set (CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -stdlib=libc++")
   endif()
endif(NOT MSVC)

################ Files ################
#   --   Add files to project.   --   #
#######################################

file(GLOB SRC_FILES
    ${CPP_DIR_1}/*.c
    ${CPP_DIR_2}/*.c
    ${CPP_DIR_3}/*.c
    ${CPP_DIR_4}/*.c
    ${CPP_DIR_5}/*.c
    ${CPP_DIR_6}/*.c
    ${CPP_DIR_7}/*.c
    ${CPP_DIR_8}/*.c
    ${CPP_DIR_9}/*.c
    ${CPP_DIR_10}/*.c
    ${CPP_DIR_11}/*.c
    ${CPP_DIR_12}/*.c
    ${CPP_DIR_13}/*.c
    ${HEADER_DIR_1}/*.h
    ${HEADER_DIR_2}/*.h
    ${HEADER_DIR_3}/*.h
    ${HEADER_DIR_4}/*.h
    ${HEADER_DIR_5}/*.h
    ${HEADER_DIR_6}/*.h
    ${HEADER_DIR_7}/*.h
    ${HEADER_DIR_8}/*.h
    ${HEADER_DIR_9}/*.h
    ${HEADER_DIR_10}/*.h
    ${HEADER_DIR_11}/*.h
    ${HEADER_DIR_12}/*.h
    ${HEADER_DIR_13}/*.h
    ${HEADER_DIR_14}/*.h
    ${HEADER_DIR_15}/*.h
)

include_directories(.)
include_directories(Piql/src)
include_directories(Piql/src/unboxer)
include_directories(Piql/src/math)
include_directories(Piql/src/base)
include_directories(Piql/src/frame)
include_directories(Piql/src/graphics)
include_directories(Piql/src/platform)
include_directories(Piql/src/codecs)
include_directories(Piql/inc)
include_directories(Piql/inc/boxing)
include_directories(Piql/inc/boxing/frame)
include_directories(Piql/inc/boxing/codecs)
include_directories(Piql/inc/boxing/graphics)
include_directories(Piql/inc/boxing/math)
include_directories(Piql/inc/boxing/platform)
include_directories(Piql/inc/boxing/unboxer)
include_directories(Piql/thirdparty)
include_directories(Piql/thirdparty/glib)
include_directories(Piql/thirdparty/bch)
include_directories(Piql/thirdparty/reedsolomon)
#include_directories(Piql/thirdparty/ldpc)

add_library(${PROJECT_NAME} STATIC ${SRC_FILES})