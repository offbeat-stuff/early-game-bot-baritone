FROM gitpod/workspace-full

RUN sudo apt-get -q update
RUN sudo apt -y install clang-format neovim

RUN bash -c "git clone https://github.com/offbeat-stuff/gitpod-dotfiles ~/dotfiles && cd ~/dotfiles && chmod +x ./install.{fish,sh} && ./install.sh"
