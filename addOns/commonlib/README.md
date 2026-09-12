# Common Library UI actions

Commonlib owns the application-action registry and the Keyboard options implementation.
The registry is the integration point for the future command palette; a palette is not
part of this change.

## Registering an action

Declare a manifest dependency on commonlib 1.45.0 or later, and an extension dependency
on `ExtensionCommonlib`. Use `ExtensionCommonlib.getActionRegistry()` in GUI mode.
Registry operations and changes to registered Swing actions must run on the EDT.
Registration can happen during the contributing extension's hook phase: the keyboard
implementation discovers existing registrations when it starts in `postInit()`.

```java
private ActionRegistry.Registration registration;

// Run on the EDT, for example through ThreadUtils.invokeAndWaitHandled in hook().
registration = commonlib.getActionRegistry().register(
        new RegisteredAction(
                "example.showDetails",
                new AbstractAction(Constant.messages.getString("example.action.showDetails")) {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        // Invoke the existing application behavior here.
                    }
                },
                null)); // No default shortcut; users can still assign one.
```

Keep the handle and call `registration.close()` on the EDT when unloading the extension.
Closing a handle twice is safe. It does not remove a later registration with the same ID.
Close change-listener handles on unload as well.

- IDs are stable, namespaced identifiers, not translated names.
- `Action.NAME` is the localized label; `Action.SHORT_DESCRIPTION` is an optional description.
- `Action.isEnabled()` is checked again at invocation. Preserve the existing action's
  mode, selection, session, and other availability checks.
- Do not register context-menu actions globally unless execution and availability can
  independently resolve the correct context.
- Registered actions without menus get main-window bindings. Normal Swing focused-component
  binding precedence still applies.
- Existing main-menu `ZapMenuItem`s are automatically adapted. Do **not** also explicitly
  register those same IDs. A single ID has one registration owner.

Consumers use `getActions()` for a snapshot, `addChangeListener()` to observe registrations
and action-property changes, and `invoke(id, event)` to execute an available action. Consumers
must not keep their own independent action lists or execution callbacks.

## Keyboard integration

`internal.keyboard` contains the legacy-menu adapter, binding resolution, main-window binding
installation, persistence, options UI, and cheatsheet API. None of these implementation classes
is an add-on API. Menu items continue to use their Swing accelerators, not a second window
binding. Nested menu additions, removals, and tab-menu replacement are observed on the EDT.

The existing `keyboard.shortcuts` configuration and menu IDs are preserved. An absent entry
means use the default; keycode zero means explicitly unbound. Explicit overrides win over
defaults, and duplicate defaults are resolved in registration order. Reassigning a shortcut clears
the previous registered owner. Reset removes overrides for registered actions, while retaining
preferences for unavailable add-ons. Options edits do not affect bindings until saved.

The `keyboard` API prefix and both cheatsheet endpoint names remain unchanged. Existing core
keyboard message keys are intentionally reused during the compatibility period, preserving
translations rather than duplicating or losing them. The new action column and help page belong
to commonlib.

## Compatibility and landing order

The registry and binding primitives can land independently of the core replacement hook.
The keyboard UI integration requires the core `ExtensionKeyboard.setShortcutProvider()` API
and declares a minimum ZAP version of 2.18.0.

Core suspends its old options, parameter set, and API before starting commonlib's provider.
Commonlib owns the replacement resources directly, **not** through its extension hook: this
prevents the extension loader from removing the restored core API during commonlib unload.
Stopping the provider saves preferences, removes listeners and window bindings, and restores
menu defaults. Core then reloads the current configuration and reactivates the legacy behavior.
Existing core and encoder callers continue through the compatibility facade.

The core fallback, facade, existing caller references, and shared translations are intentionally
retained for this release. Removing them is a later compatibility-breaking cleanup, not part of
activating the add-on replacement. Do not delete the legacy implementation before deciding the
supported old-commonlib and third-party-caller compatibility window.

## Local validation before the core API is released

Build and publish the matching core checkout to Maven local:

```sh
cd zaproxy
./gradlew :zap:publishToMavenLocal
```

Use a temporary Gradle init script in the extensions checkout to resolve the matching local
snapshot rather than the unreleased 2.18.0 release dependency:

```groovy
allprojects {
    repositories { mavenLocal() }
    configurations.configureEach {
        resolutionStrategy.dependencySubstitution {
            substitute module('org.zaproxy:zap') using module('org.zaproxy:zap:2.18.0-SNAPSHOT')
        }
    }
}
```

```sh
./gradlew -I /path/to/local-core.gradle :addOns:commonlib:test :addOns:commonlib:spotlessCheck :addOns:commonlib:jar
```

Use a Gradle-compatible JDK (21 for the current wrapper). Swing component tests also require
fontconfig and fonts, even when run headlessly. The init script is only for local validation;
it must not be used to claim that the add-on builds against the currently released core.
