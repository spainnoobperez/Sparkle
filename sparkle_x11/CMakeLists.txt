cmake_minimum_required(VERSION 3.6.0)
project(sparkle_x11)

get_filename_component(SPARKLE_ROOT "${CMAKE_CURRENT_LIST_DIR}/.." ABSOLUTE)

#add_subdirectory(${SPARKLE_ROOT}/were were)
add_subdirectory(${SPARKLE_ROOT}/sparkle_common sparkle_common)

file(GLOB EVERYTHING
    "*.cpp"
    "*.h"
)

add_executable(sparkle
    ${EVERYTHING}
)

target_include_directories(sparkle PUBLIC
    #${CMAKE_CURRENT_SOURCE_DIR}
    ${SPARKLE_ROOT}/were
    ${SPARKLE_ROOT}/sparkle_common
)

set_target_properties(sparkle PROPERTIES COMPILE_FLAGS "-Wall -Wextra -Wno-unused-parameter -std=c++11 -O2")

target_link_libraries(sparkle LINK_PUBLIC
    were
    sparkle_common
    wayland-server
    X11
)