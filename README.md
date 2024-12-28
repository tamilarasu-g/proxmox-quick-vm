# Create VMs in Proxmox like in DigitalOcean or Linode using Proxmox API

> Kotlin is used to interact with the Proxmox API

> Make sure to download the qcow2 files before running the program

## Todo
- Get input for manually creating size
- Use a simple database ( **not yet decided** ) for arranging the values of versions and files location
- Remove the delay and add status tracking before invoking next function
- Handle exceptions
- Make an easy way for downloading images
- Solve Fedora 39,40 not getting internet access
- Unit for memory is not specified when displaying sizes

## Resources
### These are all the resources that I used while creating these
- [How to use dotenv in Kotlin](https://www.dotenv.org/docs/languages/kotlin)
- [Use Cloud Init Proxmox Forum](https://forum.proxmox.com/threads/cloudinit-via-api.123811/)
- [Managing Snippets via Proxmox API - Proxmox Forum ](https://forum.proxmox.com/threads/creating-snippets-using-pve-api.54081/)
- [Request to add Snippets via Proxmox API - Bugzilla](https://bugzilla.proxmox.com/show_bug.cgi?id=2208)
- [URL Encode Strings](https://www.urlencoder.org/)
- [Proxmox API Documentation](https://pve.proxmox.com/wiki/Proxmox_VE_API)
- [Blog that I used to create the CLI](https://technotim.live/posts/cloud-init-cloud-image/)
- [Proxmox API Viewer](https://pve.proxmox.com/pve-docs/api-viewer/)
- [Arch Cloud Images](https://geo.mirror.pkgbuild.com/images/)
- [Debian Cloud Images](https://cdimage.debian.org/images/cloud/)
- [Ubuntu Cloud Images](https://cloud-images.ubuntu.com/)
- [Fedora Cloud Images](https://dl-iad03.fedoraproject.org/pub/fedora/linux/releases/)
- [Rocky Linux Cloud Images](https://dl.rockylinux.org/pub/rocky/)
- [Alma Linux Cloud Images](https://repo.almalinux.org/almalinux/)