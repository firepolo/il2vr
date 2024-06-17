#include <Logger.hpp>

#include <chrono>
#include <filesystem>
#include <format>
#include <string>

using namespace bhe;

std::ofstream Logger::stream;

void Logger::Initialize()
{
	const auto path = std::filesystem::absolute("log.txt"); 
	std::filesystem::remove(path);
	stream = std::ofstream(path.string(), std::ios_base::app);
}

void Logger::Shutdown()
{
	stream.close();
}

void Logger::Log(Level level, const std::string& file, const int line, const std::string& msg)
{
	static const std::string levelStrings[] = { "Fatal", "Error", "Warning", "Info", "Debug", "Trace" };

	stream << std::format("[{:%Y/%m/%d %H:%M:%S}][{}] file:\"{}\" line:\"{}\" message:\"{}\"", std::chrono::system_clock::now(), levelStrings[level], file, line, msg);
	stream << std::endl;
}