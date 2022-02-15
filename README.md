# Lightmans Currency
Lightman's Currency is a forge mod that adds a simple currency system of 6 different coins, as well as break & access protected traders from which players can safely set up trades with other players without fear of theft or griefing.

## Features
#### Coins
- 6 different interchangable coins (Copper, Iron, Gold, Emerald, Diamond, and Netherite), each of which can be minted or melted back into their base material at a coin mint.
- Disclaimer: Copper coins cannot be minted or melted in 1.16 without use of a datapack to add mint/melt recipes for another mods copper ingots. More info can be found [here](https://github.com/Lightman314/LightmansCurrency/tree/LC-1.16/Recipe%20Datapacks).
- Server owners can customize which coins are allowed to be minted or melted.
- Coins can be converted all different types of coins at an ATM.
#### Wallets
- 6 Craftable wallets corresponding to the 6 different coin types, each with more storage than the previous one.
- Wallets can be equipped in the wallet slot, which can be accessed by key bind ('V' by default) or by clicking a button on the top-right of the players inventory. Equipped wallets render on the players left hip.
- High enough level wallets (Iron by default) can convert their contents into the highest level coins to conserve wallet space.
- High enough level wallets (Gold by default) can automatically collect coins picked up from the ground while equipped.
- High enough level wallets (Netherite by default) can transfer coins between your personal bank account and the wallet without the need to use an ATM.
- Wallets can either be crafted directly, or upgraded from a lower level wallet at a slight inefficiency.
- Server owners can customize what level of wallet is required to access certain wallet features.
#### Bank Accounts
- Each player has a personal bank account that can store a nearly infinite amount of money (only limited by java limitations).
- Each player can transfer coins into or out of their bank account from any ATM machine.
- Team Owners can create a team bank account and define what level of member is allowed to access it via the ATM account selection.
#### Traders
- Various craftable & grief-proof traders that can be used to set up stores from which you can buy or sell items to other players.
- Multiple trade types, from sales (sell item for $$), purchases (buy item for $$), and barters (trade item for another item), each customizable on a per-trade basis.
- Team system, with which you can create a group of friends to moderate your traders together.
- Trader permission system, from which you can define what your friends can & can't do to your traders.
- Trade Rule system, from which you can set up time-limited sales, give discounts to your friends, or limit the number of purchases allowed to be made within a certain time period.
- Universal Trader system (Trading Server's) from which people can buy items from your trader from anywhere in any dimension.
- Traders can be linked to the owners bank account for automatic deposit of any funds gained, and automatic withdrawl for any purchases made. Does not work for traders owned by a team unless the team owner has created a bank account for said team.
#### Trading Teams
- Players can create and invite other players to a Team.
- Teams have 3 levels of access, member, admin, and owner.
- Any member of a team can transfer ownership of any trader they own to the Team.
- While a trader is owned by a Team all members of the team will have Ally permissions for that trader and Team Admins/Owners will have Owner permissions.
#### Administration
- Admin mode, activatable by command, allowing admins to access all features of any trader or team while active, including trader destruction.
- Creative traders can be enabled while in Admin Mode from the Trader Settings screen. While a trader is creative, it will have infinite stock for all trades carried out, but will not modify the internal coin or item storage of the trader.
- Universal Trader search feature, usable to find the dimension & coordinates of a Trading Server. Also includes a command to allow for manual deletion of a prohibited or defective universal trader.

For more details on this mods features, check the [wiki](https://github.com/Lightman314/LightmansCurrency/wiki) available on this github or leave a comment in the [Discord group](https://discord.com/invite/uVFWAshgbZ) development section (must self-assign the Development role in #roles to access).
