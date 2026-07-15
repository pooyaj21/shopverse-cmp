source "https://rubygems.org"

gem "fastlane"
gem "multi_json" # transitively required by fastlane's google-apis gems but not declared by them

plugins_path = File.join(File.dirname(__FILE__), "fastlane", "Pluginfile")
eval_gemfile(plugins_path) if File.exist?(plugins_path)
