cmake_minimum_required(VERSION 3.6.0)


#set(SPARKLE_ROOT "../../../../../")
get_filename_component(SPARKLE_ROOT "${CMAKE_CURRENT_LIST_DIR}/../../../../../" ABSOLUTE)

#add_subdirectory(${SPARKLE_ROOT}/were were)
add_subdirectory(${SPARKLE_ROOT}/sparkle_common sparkle_common)

file(GLOB EVERYTHING
    "sparkle_*.cpp"
    "sparkle_*.h"
    "were_*.cpp"
    "were_*.h"
)


add_library(sparkle SHARED
    ${EVERYTHING}
)


target_include_directories(sparkle PUBLIC
    #${CMAKE_CURRENT_SOURCE_DIR}
    ${SPARKLE_ROOT}/were
    ${SPARKLE_ROOT}/were/math
    ${SPARKLE_ROOT}/sparkle_common
    #${SPARKLE_ROOT}
    ${SPARKLE_ROOT}/foreign/install/${ANDROID_ABI}/usr/include
)

target_include_directories(sparkle_common PUBLIC
    ${SPARKLE_ROOT}/foreign/install/${ANDROID_ABI}/usr/include
)

set_target_properties(sparkle PROPERTIES COMPILE_FLAGS "-Wall -Wextra -Wno-unused-parameter -O2")
target_compile_options(sparkle PUBLIC $<$<COMPILE_LANGUAGE:CXX>:-std=c++17>)

target_link_libraries(sparkle LINK_PUBLIC
    were
    sparkle_common
    ${SPARKLE_ROOT}/foreign/install/${ANDROID_ABI}/usr/lib/libwayland-server.a
    ${SPARKLE_ROOT}/foreign/install/${ANDROID_ABI}/usr/lib/libffi.a
    android
    OpenSLES
)

