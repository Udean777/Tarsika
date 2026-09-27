# Gallery architecture boundaries

The gallery feature uses feature-first packages:

- `domain` contains gallery policies, models, repository contracts, and use cases. It must not import Android, Compose, Room, or application data implementations.
- `data` implements domain contracts. Android APIs, MediaStore, Room entities/DAOs, DataStore, file access, EXIF, and bitmap processing stay here. Mapping to domain models happens at these adapters.
- `presentation` contains route state holders, immutable screen state, Compose content, and Android user-consent flows. Screens receive state and callbacks; they do not receive ViewModels or repositories. ViewModels may depend on domain contracts and use cases, but must not import `data` implementations.
- `app.di` is the composition root and is the only place that chooses concrete gallery data adapters.

System permission, biometric, Storage Access Framework, and MediaStore consent prompts remain in presentation because Android requires an Activity or launcher to show them. The data adapters expose platform-neutral inputs and results around those flows.

Add a use case when it holds a product rule or coordinates more than one operation. Keep simple repository reads and writes as direct contract operations rather than wrapping every method.

Gallery state is grouped by user flow: device gallery, user albums, trash, and hidden album each have a focused ViewModel. `MainNavGraph` is the presentation composition boundary: it collects those state flows and passes values and callbacks to navigation screens. One-shot failures are emitted as UI effects; coroutine cancellation is rethrown.

Bulk UI components may launch platform permission or consent prompts. Normal media mutations still go through domain use cases and repository contracts. Android 10's recoverable delete consent is handled at the UI boundary because its `IntentSender` comes from `RecoverableSecurityException`; this API-specific path is guarded by the Android version.
