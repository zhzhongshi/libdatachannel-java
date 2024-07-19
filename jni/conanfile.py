from conan import ConanFile
from conan.tools.cmake import CMakeToolchain, CMakeDeps

class LibDataChannel(ConanFile):
    settings = "os", "compiler", "build_type", "arch"

    def requirements(self):
        self.requires("openssl/3.2.2")

    def generate(self):
        d = CMakeDeps(self)
        d.generate()
        tc = CMakeToolchain(self)
        tc.user_presets_path = False
        tc.generate()
