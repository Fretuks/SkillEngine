# **Skill Engine - A Modular Skill Tree Framework for Forge Mods**

**Skill Engine** is **not** a standalone progression mod.  
It does **not** add gameplay, attributes, or abilities by itself.

Instead, **Skill Engine is a framework** designed to let other mods create their own **highly customizable skill trees**, complete with:

*   Node unlocking
*   Attribute requirements
*   Custom icons
*   Skill point handling
*   Server synchronization
*   Requirements & tooltips
*   Interaction with other progression systems (like Ascend)

If you're looking for a mod that _adds_ skills, this is **not** it.  
If you are a developer who wants to **add skill trees to your mod without reinventing everything**, Skill Engine is exactly what you need.

***

# **Features**

### JSON-driven Skill Tree System

Mods can register their own skill nodes simply by adding JSON files under:

```
data/<yourmodid>/skillnodes/*.json
```

### Automatic Node Rendering

Nodes are positioned, displayed, zoomed, panned, and drawn automatically in the Skill Tree GUI.

### Icons Support

Use **your own textures** or **vanilla item textures** as node icons.

### Requirements System

Nodes can specify:

*   Linked nodes
*   Prerequisite attributes
*   Skill costs
*   Custom tags

The UI automatically displays requirements in tooltips and overlays.

### Unlock Logic & Sync

Unlocking is fully handled server-side and synced to the client.

### Full API Support

Skill Engine includes a public API class that allows other mods to:

*   Give skill points
*   Unlock skill nodes
*   Check unlock state
*   Access player skill data
*   Register or reference nodes

***

# **How to Use Skill Engine in Your Mod**

Skill Engine is designed to be extremely easy to integrate.

## **1\. Add Skill Engine as a Dependency**

In your `mods.toml`:

```toml
[[dependencies.yourmodid]]
    modId="skillengine"
    type="required"
    versionRange="[1.0.0,)"
```

In your Gradle build:

```gradle
implementation fg.deobf("net.fretux.skillengine:SkillEngine:<version>")
```

***

# **2\. Create Your Skill Nodes (JSON)**

Example:

```json
{
  "title": "Basic Strength",
  "description": "You feel your muscles tighten.",
  "cost": 1,
  "position": { "x": -150.0, "y": -50.0 },
  "links": ["skillengine:welcome"],
  "tags": ["yourmodid:strength_1"],
  "icons": "minecraft:textures/item/iron_sword.png",
  "prerequisites": {
    "strength": 10,
    "agility": 5
  }
}
```

Place this in:

```
data/yourmodid/skillnodes/basic_strength.json
```

Skill Engine will automatically detect it and add it to the tree.

***

# **3\. Use the API in Your Code**

Skill Engine exposes a simple API:

```java
SkillEngineAPI.unlockNode(player, SkillEngineAPI.rl("yourmodid", "basic_strength"));
SkillEngineAPI.addSkillPoints(player, 3);
boolean unlocked = SkillEngineAPI.isUnlocked(player, SkillEngineAPI.id("welcome"));
```

You can interact with the system without touching internal classes.

# **Users: What Does This Mod Do for Me?**

Skill Engine **does nothing by itself**.  
It does _not_ add a skill tree or abilities on its own.

You only need this mod if:

*   A mod you play **depends** on Skill Engine
*   You want to install addons that create skill trees
