from conan import ConanFile


class LibDataChannel(ConanFile):
    settings = "os", "arch", "compiler", "build_type"
    requires = "openssl/3.2.2"
    generators = "CMakeDeps"

    def configure(self):
        self.options["openssl"].shared = self.settings.os != "Windows"
