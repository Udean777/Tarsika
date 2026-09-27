# Device folder deletion plan

## Scope

Device albums are views over folders indexed by MediaStore. Deleting every photo removes the album from Tarsika's populated album list, while leaving the user's original folder alone. The existing **delete album** action applies only to app-owned `UserPhotoAlbum` collections; it does not delete a device folder.

This plan adds an explicit, separate action for deleting a device folder and its contents. It must never run as a side effect of deleting the last photo from an album.

## User behavior

1. Add a device-folder action with copy that clearly says it permanently deletes the folder's contents and the folder itself.
2. Show the folder path and the number of indexed items in the confirmation.
3. Require explicit confirmation and the Android system's consent where applicable. Cancellation leaves files and folder untouched.
4. On success, refresh the MediaStore-backed library, return to the album list if the open folder disappeared, and show only albums with at least one indexed photo.
5. If Tarsika cannot safely identify or delete the physical directory, report that and leave it intact; never silently fall back to deleting only some of its files.

## Architecture and responsibilities

- **Presentation:** add the device-folder menu and confirmation UI; launch Android consent flows (`MediaStore.createDeleteRequest` and, if needed, the Storage Access Framework picker); pass the user's result to the ViewModel. Keep this distinct from deleting an app-owned album.
- **Domain:** add a `DeleteDeviceFolderUseCase` only if it coordinates folder validation and the delete workflow. Its input should carry stable folder identity (volume plus bucket/path) and an explicit authorization result, not Android framework types. Keep Android and Compose out of domain.
- **Repository contract:** define a focused device-folder deletion contract that can resolve current folder contents and return an outcome such as deleted, changed, not found, or unsupported. Do not reuse `UserPhotoAlbumRepository.delete` or the per-photo deletion contract to imply directory deletion.
- **Data:** implement the contract with MediaStore queries scoped by volume and normalized relative path/bucket identity. Re-check the folder contents immediately before mutation so a stale album snapshot cannot delete files added to or moved into a different folder.
- **Composition root:** bind the new contract and use case in `app.di`.
- **ViewModel:** coordinate the domain operation and emit success/failure effects; preserve cancellation behavior used by the existing gallery ViewModels.

## Platform strategy to validate before implementation

Android's `MediaStore.createDeleteRequest` permanently deletes the listed media items after the user approves; it does not express a directory deletion. Use it for the folder's indexed media contents on Android 11+. Android 10 needs the existing recoverable-security-exception consent path; older Android versions need the app's supported storage permission path.

Deleting the directory entry itself needs a separate, user-authorized directory capability where the provider supports it. Evaluate `ACTION_OPEN_DOCUMENT_TREE` / SAF `DocumentsContract` behavior for the selected volume and Android versions, including restricted roots and providers that reject directory deletion. Require the picked tree to match the target folder identity before deleting. Do not use raw filesystem deletion or `MANAGE_EXTERNAL_STORAGE` as a shortcut. If the system/provider cannot authorize or perform a true directory deletion, stop before deleting contents and explain the limitation.

The system delete request and a SAF recursive delete must not both run on the same files. Choose and validate one deletion route per platform/provider so a successful request is not followed by a duplicate delete.

## Implementation sequence

1. Confirm product wording and supported Android/provider behavior for deleting a physical directory.
2. Add a stable `DeviceFolderIdentity` domain model and repository contract; cover stale identity and unsupported-provider outcomes.
3. Implement scoped MediaStore lookup and platform adapters without exposing Android types through domain.
4. Add the use case and dependency injection binding.
5. Add the explicit folder action, confirmation, consent launchers, and result handling in presentation.
6. Refresh the gallery after success; keep the existing last-item behavior: the device album disappears from the in-app list and the detail screen navigates back, while the physical folder remains unless the explicit folder action was used.
7. Verify on Android 10, Android 11+, a removable volume if supported, cancellation, stale contents, and SAF providers that cannot delete directories.

## Acceptance criteria

- Deleting the last photo from an album never invokes folder deletion; its device-album card disappears and an open detail screen returns to the album list.
- Empty device albums are never rendered.
- Only an explicit confirmed folder action can attempt physical folder deletion.
- Denied consent, unsupported providers, stale identities, and partial/ambiguous outcomes do not report success or silently leave a partially deleted folder.
- Deleting an app-owned user album continues to affect only Tarsika's local album metadata.
