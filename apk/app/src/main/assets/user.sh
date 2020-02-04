
source /data/data/com.sion.sparkle/files/sparkle.sh

function user_start()
{
    #start_generic_container source="/dev/block/mmcblk0p36" point="/data/local/tmp/fedora" user="sion"
}

function start_generic_container()
{
    local $*

    check_root

    exists "${point?}/bin/su" || check_mount ${point?} -o noatime ${source?}

    critical "cd ${point?}"

    check_mount dev -o bind /dev
    check_mount dev/pts -t devpts devpts
    check_mount proc -t proc proc
    check_mount sys -t sysfs sysfs
    check_mount tmp -t tmpfs tmpfs
    check_mount dev/shm -t tmpfs tmpfs
    check_mount data -o bind /data

    optional "chcon u:object_r:app_data_file:s0 tmp"

    exists "tmp/sparkle/wayland-0" ||
    {
        sparkle_chroot ${user?} "mkdir -p /tmp/sparkle"
        critical "busybox ln -s /data/data/com.sion.sparkle/files/wayland-0 tmp/sparkle/wayland-0"
    }

    is_running "Xwayland" ||
    {
        sparkle_chroot ${user?} "XDG_RUNTIME_DIR=/tmp/sparkle Xwayland :0 -noreset &"
        critical "busybox sleep 1"
        sparkle_chroot ${user?} "DISPLAY=:0 /bin/sh ~/.xinitrc &"
        critical "busybox sleep 1"

    }
}

user_start
