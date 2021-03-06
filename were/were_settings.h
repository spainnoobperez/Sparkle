#ifndef WERE_SETTINGS_H
#define WERE_SETTINGS_H

#include "were_object.h"
#include <string>
#include <variant>
#include <map>
#include <regex>
#include <mutex>


struct were_settings_handler
{
    std::regex re;
    std::function<void (const std::smatch &match)> f;
};

class were_settings : virtual public were_object
{
public:
    ~were_settings() override;
    explicit were_settings(const std::string &path);

    void load();

    template <typename T>
    T get(const std::string &key, const T &default_value)
    {
        mutex_.lock();

        auto it = settings_.find(key);
        if (it == settings_.end())
        {
            mutex_.unlock();

            return default_value;
        }

        T value = std::get<T>(it->second);

        mutex_.unlock();

        return value;
    }

private:
    void register_handler(const std::string &pattern,
        const std::function<void (const std::smatch &match)> &handler);
    void process_line(const std::string &line);

private:
    std::string path_;
    std::map<std::string, std::variant<std::string, bool, int, double>> settings_;
    std::vector<were_settings_handler> handlers_;
    std::mutex mutex_;
};

#endif // WERE_SETTINGS_H
