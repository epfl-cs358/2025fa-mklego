from core.stl_loader import load_stl
from core.voxelizer import voxelize
from core.lego_mapper import map_to_lego
from core.gcode_generator import generate_lego_gcode

# Entry point: parse arguments, load STL, run pipeline
def main():
    mesh = load_stl("examples/sample.stl")
    voxels = voxelize(mesh, resolution=0.008)
    lego_blocks = map_to_lego(voxels)
    generate_lego_gcode(lego_blocks, "output/build.lego")

if __name__ == "__main__":
    main()
