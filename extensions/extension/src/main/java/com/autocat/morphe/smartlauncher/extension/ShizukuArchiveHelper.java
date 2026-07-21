package com.autocat.morphe.smartlauncher.extension;

/**
 * Placeholder. Not implemented.
 * <p>
 * The previous version of this patch assumed {@code Shizuku.newProcess(String[], String[], String)}
 * could be called directly to run "pm archive &lt;package&gt;" as a shell command.
 * That method is {@code private} in the current Shizuku API (rikka.shizuku:api,
 * confirmed against the real RikkaApps/Shizuku-API source) and is documented as
 * deprecated / planned for removal - it was never actually callable from app
 * code, in any version.
 * <p>
 * A real implementation needs a bound Shizuku UserService: a small AIDL-backed
 * service class that runs with the Shizuku-granted UID and calls the privileged
 * archiving API (e.g. IPackageInstaller/IPackageManager via reflection, or
 * simply shells out to "pm archive" from inside the service process) - see
 * {@code Shizuku.bindUserService(UserServiceArgs, ServiceConnection)} in the
 * Shizuku API docs. That's a self-contained, well-documented pattern, just not
 * something to fabricate without building and testing the actual service.
 */
@SuppressWarnings("unused")
public class ShizukuArchiveHelper {
}
