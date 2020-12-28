# `conveyor`

`conveyor` is a CLI for uploading local imagery into a new Raster Foundry project.

The CLI is published under `jisantuc/conveyor`. You can obtain the container with
`docker pull jisantuc/conveyor:latest`, then:

```bash
$ docker run jisantuc/conveyor:latest
Usage: conveyor new-project [--datasource <uuid>] <PROJECT_NAME> <TIFF_ABSOLUTE_PATH> <REFRESH_TOKEN>

Upload an image to a new Raster Foundry project

Options and flags:
    --help
        Display this help text.
    --datasource <uuid>
        Which datasource to associate with this imagery
```

The program's arguments are:

- `PROJECT_NAME`: the name to associate with your new project
- `TIFF_ABSOLUTE_PATH`: a path to a GeoTIFF in the container's file system
- `REFRESH_TOKEN`: the refresh token to use to obtain a JSON Web Token from the Raster Foundry API

It also optionally takes a datasource id in the `--datasource` option. If absent, the program will supply
the id for a default 4 band datasource.

A complete example with a bogus refresh token is shown below, including mounting the local directory with
data into the container:

```bash
$ docker run \
    -v $(pwd)/data:/opt/data \
    jisantuc/conveyor:latest \
    "Test project" \
    /opt/data/upload.tif \
    "refresh-token" \
    --datasource=001d8582-376b-4d18-a93e-01ffe8fb0da8 # replace with a real datasource ID if desired
```

Due to some kind of poorly understood behavior with connection channels,
the program won't exit on its own, but you can `ctrl+c` it to kill
it once you've seen `Waiting for upload completion` and `exhausted input` in the logs.

## Obtaining a refresh token

To obtain a refresh token, follow instructions on the Raster Foundry [help page](https://help.rasterfoundry.com/en/articles/777804-generating-a-refresh-token-in-order-to-use-the-api).
While it notes you can only view a given refresh token once, you can create as many as you need, so don't panic if you accidentally
click out of the modal before you copy the token. For ease of access, consider storing your refresh token in a password manager.

## Finding a datasource ID

You can find datasources in the ["Datasources" tab](https://app.rasterfoundry.com/imports/datasources/list?page=1) of the Raster Foundry data
management UI. To find the ID for a given datasource, you can click the eye icon next to its name and copy its id from the URL.
