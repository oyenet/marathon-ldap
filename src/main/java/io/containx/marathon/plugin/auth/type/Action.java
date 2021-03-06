package io.containx.marathon.plugin.auth.type;

import mesosphere.marathon.plugin.auth.AuthorizedAction;
import mesosphere.marathon.plugin.auth.*;

/**
 * Enumeration for handling AuthorizedActions
 */
public enum Action {
    // App Actions
    CREATE_APP(CreateApp$.MODULE$, PermissionType.CREATE, EntityType.APP),
    UPDATE_APP(UpdateApp$.MODULE$, PermissionType.UPDATE, EntityType.APP),
    DELETE_APP(DeleteApp$.MODULE$, PermissionType.DELETE, EntityType.APP),
    VIEW_APP(ViewApp$.MODULE$, PermissionType.VIEW, EntityType.APP),

    // Group Actions
    CREATE_GROUP(CreateGroup$.MODULE$, PermissionType.CREATE, EntityType.GROUP),
    UPDATE_GROUP(UpdateGroup$.MODULE$, PermissionType.UPDATE, EntityType.GROUP),
    DELETE_GROUP(DeleteGroup$.MODULE$, PermissionType.DELETE, EntityType.GROUP),
    VIEW_GROUP(ViewGroup$.MODULE$, PermissionType.VIEW, EntityType.GROUP),
    ;
    private final AuthorizedAction<?> action;
    private final EntityType entityType;
    private final PermissionType permType;

    Action(AuthorizedAction<?> action, PermissionType permType, EntityType entityType) {
        this.action = action;
        this.permType = permType;
        this.entityType = entityType;
    }

    public static Action byAction(AuthorizedAction<?> action) {
        for (Action a : values()) {
            if (a.action.equals(action)) {
                return a;
            }
        }
        throw new IllegalArgumentException("Unknown Action: " + action);
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public PermissionType getPermType() {
        return permType;
    }
}
