#pragma once

#include <fstream>

#define BHE_WARNING_ENABLED 1
#define BHE_INFO_ENABLED 1
#define BHE_DEBUG_ENABLED 1
#define BHE_TRACE_ENABLED 1

#define BHE_FATAL_ASSERT(exp, msg) if (exp) bhe::Logger::Log(bhe::Logger::Level::Fatal, __FILE__, __LINE__, msg);
#define BHE_FATAL(msg) bhe::Logger::Log(bhe::Logger::Level::Fatal, __FILE__, __LINE__, msg);
#define BHE_ERROR(msg) bhe::Logger::Log(bhe::Logger::Level::Error, __FILE__, __LINE__, msg);

#if BHE_WARNING_ENABLED == 1
	#define BHE_WARNING(msg) bhe::Logger::Log(bhe::Logger::Level::Warning, __FILE__, __LINE__, msg);
#else
	#define BHE_WARNING(msg)
#endif

#if BHE_INFO_ENABLED == 1
	#define BHE_INFO(msg) bhe::Logger::Log(bhe::Logger::Level::Info, __FILE__, __LINE__, msg);
#else
	#define BHE_INFO(msg)
#endif

#if BHE_DEBUG_ENABLED == 1
	#define BHE_DEBUG(msg) bhe::Logger::Log(bhe::Logger::Level::Debug, __FILE__, __LINE__, msg);
#else
	#define BHE_DEBUG(msg)
#endif

#if BHE_TRACE_ENABLED == 1
	#define BHE_TRACE(msg) bhe::Logger::Log(bhe::Logger::Level::Trace, __FILE__, __LINE__, msg);
#else
	#define BHE_TRACE(msg)
#endif

namespace bhe
{
	class Logger
	{
	public:
		enum Level
		{
			Fatal,
			Error,
			Warning,
			Info,
			Debug,
			Trace
		};

		static void Initialize();
		static void Shutdown();
		static void Log(Level level, const std::string& file, const int line, const std::string& msg);

	private:
		static std::ofstream stream;
	};
}