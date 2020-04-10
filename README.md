# `conveyor`

`conveyor` is a CLI for uploading local imagery into a new Raster Foundry project.

If you have the build jar of this CLI, you can run:

```bash
$ java -jar cli-assembly-0.1.0-SNAPSHOT.jar new-project
Usage: conveyor new-project [--datasource <uuid>] <PROJECT_NAME> <TIFF_ABSOLUTE_PATH> <REFRESH_TOKEN>

Upload an image to a new Raster Foundry project

Options and flags:
    --help
        Display this help text.
    --datasource <uuid>
        Which datasource to associate with this imagery
```

If you're developing and have [`bloop`](https://scalacenter.github.io/bloop/) on your
path, you can run the above as:

```bash
$ bloop run cli -- new-project
```

Output will be the same.

The program's arguments are:

- `PROJECT_NAME`: the name to associate with your new project
- `TIFF_ABSOLUTE_PATH`: a path to a GeoTIFF on your local file system
- `REFRESH_TOKEN`: the refresh token to use to obtain a JSON Web Token from the Raster Foundry API

It also optionally takes a datasource id in the `--datasource` option. If absent, the program will supply
the id for a default 4 band datasource.

For example, to create a project called "Good Imagery" with the file `good-imagery.tiff` in your home directory,
you would run:

```bash
$ java -jar cli-assembly-0.1.0-SNAPSHOT.jar new-project "Good Imagery" $HOME/good-imagery.tiff abcdefg
```