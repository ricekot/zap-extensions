# ZAP Web UI

This is a React based web UI for ZAP. It is a work in progress and is not yet ready for general use.

It uses Tailwind CSS for styling and React Router for navigation.

## Development

To run the development server:

```shell
npm run dev
```

## Build

To build the web UI for the webuipoc add-on,

```shell
npm run build:forAddOn
```

And then copy or install the add-on as usual from the zap-extensions directory,

```shell
./gradlew addOns:webuipoc:copyZapAddOn
# or: ./gradlew addOns:webuipoc:installZapAddOn to install it into the running ZAP instance
```

Building for the add-on is slightly different to the normal build as it uses a different basename (`/saturn/`).

## References

- https://github.com/Sridhar-C-25/sidebar_reactTailwind/tree/main
- https://reactrouter.com/en/main/start/tutorial
- https://tailwindcomponents.com/component/404-page-not-found
- https://github.com/Microsoft/monaco-editor/issues/604#issuecomment-344214706
