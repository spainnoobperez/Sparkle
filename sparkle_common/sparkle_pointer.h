#ifndef SPARKLE_POINTER_H
#define SPARKLE_POINTER_H

#include "sparkle.h"
#include "generated/sparkle_wl_pointer.h"
#include "sparkle_surface.h"
#include <linux/input-event-codes.h>


class sparkle_pointer : public sparkle_wl_pointer
{
public:
    sparkle_pointer(struct wl_client *client, int version, uint32_t id, were_pointer<sparkle_display> display) :
        sparkle_wl_pointer(client, version, id), display_(display)
    {
    }

    void button_down(int button)
    {
        if (button == BTN_GEAR_UP)
            wheel_up();
        else if (button == BTN_GEAR_DOWN)
            wheel_down();
        else
            send_button(sparkle::next_serial(display_), sparkle::current_msecs(), button, WL_POINTER_BUTTON_STATE_PRESSED);
    }

    void button_up(int button)
    {
        if (button == BTN_GEAR_UP || button == BTN_GEAR_DOWN) {}
        else
            send_button(sparkle::next_serial(display_), sparkle::current_msecs(), button, WL_POINTER_BUTTON_STATE_RELEASED);
    }

    void motion(int x, int y)
    {
        send_motion(sparkle::current_msecs(), wl_fixed_from_int(x), wl_fixed_from_int(y));
        if (version() >= WL_POINTER_FRAME_SINCE_VERSION)
            send_frame();
    }

    void enter(were_pointer<sparkle_surface> surface)
    {
        send_enter(sparkle::next_serial(display_), surface->resource(), wl_fixed_from_int(0), wl_fixed_from_int(0));
        if (version() >= WL_POINTER_FRAME_SINCE_VERSION)
            send_frame();
    }

    void leave(were_pointer<sparkle_surface> surface)
    {
        send_leave(sparkle::next_serial(display_), surface->resource());
        if (version() >= WL_POINTER_FRAME_SINCE_VERSION)
            send_frame();
    }

    void wheel_down()
    {
        send_axis(sparkle::current_msecs(), 0, wl_fixed_from_double(10.0));
    }

    void wheel_up()
    {
        send_axis(sparkle::current_msecs(), 0, wl_fixed_from_double(-10.0));
    }

private:
    were_pointer<sparkle_display> display_;
};

#endif // SPARKLE_POINTER_H
